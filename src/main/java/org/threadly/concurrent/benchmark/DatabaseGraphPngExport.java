package org.threadly.concurrent.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.CategorySeries;
import org.knowm.xchart.CategorySeries.CategorySeriesRenderStyle;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.Styler.TextAlignment;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.skife.jdbi.v2.DBI;
import org.threadly.concurrent.benchmark.dao.BenchmarkDao;
import org.threadly.concurrent.benchmark.dao.RunRecord;
import org.threadly.util.StringUtils;

public class DatabaseGraphPngExport {
  private static final boolean EXPORT_GRAPHS = true;
  private static final int CHART_WIDTH = 1440;
  private static final int CHART_HEIGHT = 900;
  private static final int JAVA_BASELINE_LAST_BENCHMARK_GROUP = 8;
  
  public static void main(String[] args) throws Exception {
    if (args.length != 6) {
      printUsage();
      System.exit(-1);
    }
    Class.forName("org.postgresql.Driver");
    DBI dbi = new DBI("jdbc:postgresql://" + args[0] + '/' + args[3],
                      args[1], args[2]);
    BenchmarkDao dao = dbi.onDemand(BenchmarkDao.class);
    
    File outputFolder = new File(args[4]);
    if (! outputFolder.exists() && ! outputFolder.mkdirs()) {
      System.err.println("Could not create directory: " + args[4]);
      printUsage();
      System.exit(-2);
    } else if (outputFolder.exists() && ! outputFolder.isDirectory()) {
      System.err.println("Output directory is not a directory: " + args[4]);
      printUsage();
      System.exit(-3);
    }
    
    if (args[5].equalsIgnoreCase("ByThreads")) {
      new ScehdulerPerformanceByThreads().generateGraphs(dao, outputFolder);
    } else if (args[5].equalsIgnoreCase("Master")) {
      new ScehdulerPerformanceOverTime("master").generateGraphs(dao, outputFolder);
    } else if (args[5].equalsIgnoreCase("Releases")) {
      new ScehdulerPerformanceOverTime("release").generateGraphs(dao, outputFolder);
    } else {
      System.err.println("Unknown generation mode: " + args[5]);
      printUsage();
      System.exit(-4);
    }
  }
  
  private static void printUsage() {
    System.err.println("java -cp <classpath> " + DatabaseGraphPngExport.class + 
                         " <dbHost> <dbUser> <dbPass> <dbName> <outputDirectory> <ByThreads|Master|Releases>");
  }
  
  private static abstract class AbstractGraphGenerator {
    public abstract void generateGraphs(BenchmarkDao dao, File outputFolder) throws Exception;
    
    protected static void chartGenerated(CategoryChart chart, String writeLocation) throws IOException {
      if (StringUtils.isNullOrEmpty(writeLocation)) {
        new SwingWrapper<CategoryChart>(chart).displayChart();
        System.in.read();
      } else {
        BitmapEncoder.saveBitmap(chart, writeLocation, BitmapFormat.PNG);
      }
    }
  }
  
  private static class ScehdulerPerformanceByThreads extends AbstractGraphGenerator {
    private static final int AVERAGE_COUNT = 2;
    private static final int MAX_BENCHMARK_GROUP_ID = 14;
    private static final List<Integer> X_AXIS_VALUES;
    private static final Pattern THREAD_COUNT_PATTERN;
    
    static {
      X_AXIS_VALUES = Arrays.asList(new Integer[] { 4, 10, 20, 50, 100, 150, 200, 250, 500, 750, 
                                                    1000, 1500, 2000, 2500});
      THREAD_COUNT_PATTERN = Pattern.compile("[0-9]+$");
    }

    @Override
    public void generateGraphs(BenchmarkDao dao, File outputFolder) throws IOException {
      int maxClassGroupId = dao.getMaxClassGroupId();
      for (int cg = 1; cg < maxClassGroupId; cg++) {
        String identifier = dao.getClassGroupIdentifier(cg);
        if (StringUtils.isNullOrEmpty(identifier)) {
          identifier = "cg:" + cg;
        }
        
        int minBenchmarkGroupId = dao.getMinBenchmarkGroupId(cg);

        for (int m = 0; m < 2; m++) {
          CategoryChart chart = new CategoryChartBuilder().width(CHART_WIDTH)
                                                          .height(CHART_HEIGHT)
                                                          .theme(ChartTheme.GGPlot2)
                                                          .title(identifier)
                                                          .xAxisTitle("Threads")
                                                          .yAxisTitle("Executions")
                                                          .build();
          chart.getStyler().setDefaultSeriesRenderStyle(CategorySeriesRenderStyle.Line);
          chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
          chart.getStyler().setAvailableSpaceFill(0);
          chart.getStyler().setOverlapped(true);
          
          for (int bg = minBenchmarkGroupId; bg <= MAX_BENCHMARK_GROUP_ID; bg++) {
            if (bg % 2 == m) {
              Integer[] executions = new Integer[X_AXIS_VALUES.size()];
              int[] count = new int[executions.length];
              for (RunRecord rr : dao.getLastResultsForBenchmarkGroupId(bg, cg, AVERAGE_COUNT)) {
                Matcher match = THREAD_COUNT_PATTERN.matcher(rr.getBenchmarkName());
                if (! match.find()) {
                  throw new IllegalStateException("Unexpected benchmark name: " + 
                                                    rr.getBenchmarkName());
                }
                Integer threads = Integer.parseInt(match.group());
                int index = X_AXIS_VALUES.indexOf(threads);
                if (index < 0) {
                  throw new IllegalStateException("Could not find x axis value: " + threads + 
                                                    " / " + rr.getBenchmarkName());
                }
                if (count[index]++ == 0) {
                  executions[index] = rr.getTotalExecutions();
                } else {
                  executions[index] += (rr.getTotalExecutions() - executions[index]) / count[index];
                }
              }
              
              CategorySeries series = chart.addSeries(dao.getBenchmarkGroupIdentifier(bg), 
                                                      X_AXIS_VALUES, Arrays.asList(executions));
              series.setMarker(SeriesMarkers.NONE);
            }
          }
          
          chartGenerated(chart, 
                         EXPORT_GRAPHS ? 
                           new File(outputFolder, identifier).getAbsolutePath() : null);
        }
      }
    }
  }
  
  private static class ScehdulerPerformanceOverTime extends AbstractGraphGenerator {
    private final String branchPrefix;
    
    public ScehdulerPerformanceOverTime(String branchPrefix) {
      this.branchPrefix = branchPrefix;
    }
    
    @Override
    public void generateGraphs(BenchmarkDao dao, File outputFolder) throws IOException {
      int maxClassGroupId = dao.getMaxClassGroupId();
      for (int cg = 0; cg < maxClassGroupId; cg++) {
        int minBenchmarkGroupId = dao.getMinBenchmarkGroupId(cg);
        if (minBenchmarkGroupId < JAVA_BASELINE_LAST_BENCHMARK_GROUP) {
          continue;
        }
        int maxBenchmarkGroupId = dao.getMaxBenchmarkGroupId(cg);
        
        for (int bg = minBenchmarkGroupId; bg <= maxBenchmarkGroupId; bg++) {
          List<RunRecord> results = dao.getResultsForBenchmarkGroupId(bg, cg);
          if (results.isEmpty()) {
            continue;
          }
          List<String> benchmarkNames = dao.getBenchmarkNamesForBenchmarkGroupId(bg, cg);
          Collections.sort(benchmarkNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
              int lengthDiff = o1.length() - o2.length();
              if (lengthDiff != 0) {
                return lengthDiff;
              } else {
                return o1.compareTo(o2);
              }
            }
          });
          
          String identifier = dao.getBenchmarkGroupIdentifier(bg);
          if (StringUtils.isNullOrEmpty(identifier)) {
            identifier = dao.getClassGroupIdentifier(cg) + "-" + bg;
            if (StringUtils.isNullOrEmpty(identifier)) {
              identifier = "cg:" + cg + "bg:" + bg;
            }
          }

          CategoryChart chart = new CategoryChartBuilder().width(CHART_WIDTH)
                                                          .height(CHART_HEIGHT)
                                                          .theme(ChartTheme.GGPlot2)
                                                          .title(identifier)
                                                          .xAxisTitle("Version")
                                                          .yAxisTitle("Executions")
                                                          .build();
          chart.getStyler().setDefaultSeriesRenderStyle(CategorySeriesRenderStyle.Line);
          chart.getStyler().setXAxisLabelRotation(270);
          chart.getStyler().setAvailableSpaceFill(0);
          chart.getStyler().setOverlapped(true);
          if (benchmarkNames.size() < 2) {
            chart.getStyler().setLegendVisible(false);
          } else {
            chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
          }
          chart.getStyler().setYAxisLabelAlignment(TextAlignment.Right);
          
          for (String name : benchmarkNames) {
            List<RunRecord> individualResults = new ArrayList<RunRecord>(results.size() / 8);
            List<String> releases = new ArrayList<String>(results.size() / 8);
            List<Integer> executions = new ArrayList<Integer>(results.size() / 8);
            for (RunRecord rr : results) {
              if (rr.getBenchmarkName().equals(name) && 
                  rr.getBranchName().startsWith(branchPrefix)) {
                individualResults.add(rr);
                
                String xIdentifier;
                if (branchPrefix.startsWith("release")) {
                  xIdentifier = rr.getBranchName();
                } else {
                  xIdentifier = new Date(rr.getRunTimestamp()).toString();
                }
                
                if (! releases.isEmpty() && releases.get(releases.size() - 1).equals(xIdentifier)) {
                  executions.set(executions.size() - 1, 
                                 (executions.get(executions.size() - 1) + rr.getTotalExecutions()) / 2);
                } else {
                  releases.add(xIdentifier);
                  executions.add(rr.getTotalExecutions());
                }
              }
            }
            
            CategorySeries series = chart.addSeries(name.length() == identifier.length() ? 
                                                      name : "Threads:" + name.replaceAll(identifier, ""), 
                                                    releases, executions);
            series.setMarker(SeriesMarkers.NONE);
          }
          
          chartGenerated(chart, 
                         EXPORT_GRAPHS ? 
                           new File(outputFolder, identifier).getAbsolutePath() : null);
        }
      }
    }
  }
}

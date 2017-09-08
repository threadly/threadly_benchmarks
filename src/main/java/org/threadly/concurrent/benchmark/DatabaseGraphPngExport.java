package org.threadly.concurrent.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    if (args[5].equalsIgnoreCase("ByThreadsPerClass")) {
      new ScehdulerPerformanceByThreadsPerClass().generateGraphs(dao, outputFolder);
    } else if (args[5].equalsIgnoreCase("ByThreadsPerType")) {
      new ScehdulerPerformanceByThreadsPerType().generateGraphs(dao, outputFolder);
    } else if (args[5].equalsIgnoreCase("master")) {
      new ScehdulerPerformanceOverTime("master").generateGraphs(dao, outputFolder);
    } else if (args[5].equalsIgnoreCase("release")) {
      new ScehdulerPerformanceOverTime("release").generateGraphs(dao, outputFolder);
    } else {
      System.err.println("Unknown generation mode: " + args[5]);
      printUsage();
      System.exit(-4);
    }
  }
  
  private static void printUsage() {
    System.err.println("java -cp <classpath> " + DatabaseGraphPngExport.class + 
                         " <dbHost> <dbUser> <dbPass> <dbName> <outputDirectory>" + 
                         " <ByThreadsPerClass|ByThreadsPerType|Master|Release>");
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
  
  private static abstract class AbstractByThreadsGraphGenerator extends AbstractGraphGenerator {
    protected static final int AVERAGE_COUNT = 2;
    protected static final List<Integer> X_AXIS_VALUES;
    protected static final Pattern THREAD_COUNT_PATTERN;
    
    static {
      X_AXIS_VALUES = Arrays.asList(new Integer[] { 4, 8, 16, 32, 64, 128, 200, 250, 500, 750, 
                                                    1000, 1500, 2000, 2500});
      THREAD_COUNT_PATTERN = Pattern.compile("[0-9]+$");
    }
  }
  
  private static class ScehdulerPerformanceByThreadsPerClass extends AbstractByThreadsGraphGenerator {
    // benchmarks not broken down by thread
    protected static final List<Integer> EXCLUDED_BENCHMARK_GROUPS = 
        Arrays.asList(new Integer[]{17, 18, 19, 20, 21, 22, 46, 49, 50, 51});
    
    @Override
    public void generateGraphs(BenchmarkDao dao, File outputFolder) throws IOException {
      int maxClassGroupId = dao.getMaxClassGroupId();
      for (int cg = 1; cg < maxClassGroupId; cg++) {
        String identifier = dao.getClassGroupIdentifier(cg);
        if (StringUtils.isNullOrEmpty(identifier)) {
          identifier = "cg:" + cg;
        }
        
        int minBenchmarkGroupId = dao.getMinBenchmarkGroupId(cg);
        int maxBenchmarkGroupId = dao.getMaxBenchmarkGroupId(cg);

        for (int m = 0; m < 2; m++) {
          String chartName = identifier + (m == 0 ? "NoOp" : "");
          CategoryChart chart = new CategoryChartBuilder().width(CHART_WIDTH)
                                                          .height(CHART_HEIGHT)
                                                          .theme(ChartTheme.GGPlot2)
                                                          .title(chartName)
                                                          .xAxisTitle("Threads")
                                                          .yAxisTitle("Executions")
                                                          .build();
          chart.getStyler().setDefaultSeriesRenderStyle(CategorySeriesRenderStyle.Line);
          chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
          chart.getStyler().setAvailableSpaceFill(0);
          chart.getStyler().setOverlapped(true);
          
          int seriesCount = 0;
          for (int bg = minBenchmarkGroupId; bg <= maxBenchmarkGroupId; bg++) {
            if (EXCLUDED_BENCHMARK_GROUPS.contains(bg)) {
              continue;
            }
            if (bg % 2 == m) {
              Integer[] executions = new Integer[X_AXIS_VALUES.size()];
              int[] count = new int[executions.length];
              for (RunRecord rr : dao.getLastResultsForBenchmarkGroupId(bg, cg)) {
                Matcher match = THREAD_COUNT_PATTERN.matcher(rr.getBenchmarkName());
                if (! match.find()) {
                  throw new IllegalStateException("Unexpected benchmark name: " + 
                                                    rr.getBenchmarkName() + " - " + bg);
                }
                Integer threads = Integer.parseInt(match.group());
                int index = X_AXIS_VALUES.indexOf(threads);
                if (index < 0) {
                  throw new IllegalStateException("Could not find x axis value: " + threads + 
                                                    " / " + rr.getBenchmarkName() + " - " + bg);
                }
                if (count[index] == 0) {
                  count[index]++;
                  executions[index] = rr.getTotalExecutions();
                } else if (count[index] < AVERAGE_COUNT) {
                  count[index]++;
                  executions[index] += (rr.getTotalExecutions() - executions[index]) / count[index];
                } else {
                  boolean done = true;
                  for (int i = 0; i < count.length; i++) {
                    if (count[i] < AVERAGE_COUNT) {
                      done = false;
                      break;
                    }
                  }
                  if (done) {
                    break;
                  }
                }
              }
              
              if (count[0] > 0) {
                seriesCount++;
                CategorySeries series = chart.addSeries(dao.getBenchmarkGroupIdentifier(bg), 
                                                        X_AXIS_VALUES, Arrays.asList(executions));
                series.setMarker(SeriesMarkers.NONE);
              }
            }
          }
          
          if (seriesCount > 0) {
            chartGenerated(chart, 
                           EXPORT_GRAPHS ? 
                             new File(outputFolder, chartName).getAbsolutePath() : null);
          }
        }
      }
    }
  }
  
  private static class ScehdulerPerformanceByThreadsPerType extends AbstractByThreadsGraphGenerator {
    @Override
    public void generateGraphs(BenchmarkDao dao, File outputFolder) throws IOException {
      List<TypeChartData> chartData = Arrays.asList(new TypeChartData(outputFolder, 
                                                                      "Execute", "execute"), 
                                                    new TypeChartData(outputFolder, 
                                                                      "Schedule", "schedule"), 
                                                    new TypeChartData(outputFolder, 
                                                                      "Recurring", "recurring"), 
                                                    new TypeChartData(outputFolder, 
                                                                      "ExecuteNoOp", "executeNoOp"), 
                                                    new TypeChartData(outputFolder, 
                                                                      "ScheduleNoOp", "scheduleNoOp"), 
                                                    new TypeChartData(outputFolder, 
                                                                      "RecurringNoOp", "recurringNoOp"));
      int maxClassGroupId = dao.getMaxClassGroupId();
      for (int cg = 1; cg < maxClassGroupId; cg++) {
        int minBenchmarkGroupId = dao.getMinBenchmarkGroupId(cg);
        int maxBenchmarkGroupId = dao.getMaxBenchmarkGroupId(cg);
        for (int bg = minBenchmarkGroupId; bg <= maxBenchmarkGroupId; bg++) {
          for (RunRecord rr : dao.getLastResultsForBenchmarkGroupId(bg, cg)) {
            boolean allComplete = true;
            for (TypeChartData tcd : chartData) {
              allComplete &= tcd.acceptRecord(rr);
            }
            if (allComplete) {
              break;
            }
          }
        }
      }
      for (TypeChartData tcd : chartData) {
        tcd.generateGraphs();
      }
    }
    
    private static class TypeChartData {
      private static final String[] CLASSES = new String[]{ "JavaUtilConcurrentScheduler", 
                                                            "JavaUtilConcurrentExecutor", 
                                                            "PriorityScheduler", "UnfairExecutor" };
      private final File outputFolder;
      private final Pattern chartTypePattern;
      private final String chartName;
      private final CategoryChart chart;
      private final Map<String, SeriesData> seriesData;
      private boolean dataComplete = false;
      
      private TypeChartData(File outputFolder, String type, String chartName) {
        this.outputFolder = outputFolder;
        seriesData = new HashMap<String, SeriesData>();
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (String s : CLASSES) {
          seriesData.put(s, new SeriesData(s));
          
          if (sb.length() > 1) {
            sb.append('|');
          }
          sb.append(s);
        }
        sb.append(')').append(type).append("[0-9]+");
        this.chartTypePattern = Pattern.compile(sb.toString());
        this.chartName = chartName;
        chart = new CategoryChartBuilder().width(CHART_WIDTH)
                                          .height(CHART_HEIGHT)
                                          .theme(ChartTheme.GGPlot2)
                                          .title(chartName)
                                          .xAxisTitle("Threads")
                                          .yAxisTitle("Executions")
                                          .build();
        chart.getStyler().setDefaultSeriesRenderStyle(CategorySeriesRenderStyle.Line);
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        chart.getStyler().setAvailableSpaceFill(0);
        chart.getStyler().setOverlapped(true);
      }
      
      private boolean dataComplete() {
        if (dataComplete) {
          return true;
        }
        for (String s : CLASSES) {
          if (! seriesData.get(s).dataComplete()) {
            return false;
          }
        }
        return dataComplete = true;
      }
      
      @SuppressWarnings("unused")
      public boolean acceptRecord(RunRecord rr) {
        if (! chartTypePattern.matcher(rr.getBenchmarkName()).matches()) {
          return dataComplete;
        } else if (dataComplete) {
          return true;
        }
        
        SeriesData seriesData = null;
        for (String s : CLASSES) {
          if (rr.getBenchmarkName().startsWith(s)) {
            seriesData = this.seriesData.get(s);
          }
        }
        if (seriesData == null) {
          throw new IllegalStateException("Unexpected run record: " + rr.getBenchmarkName());
        }
        Matcher match = THREAD_COUNT_PATTERN.matcher(rr.getBenchmarkName());
        if (! match.find()) {
          throw new IllegalStateException("Unexpected benchmark name: " + rr.getBenchmarkName());
        }
        Integer threads = Integer.parseInt(match.group());
        int index = X_AXIS_VALUES.indexOf(threads);
        if (index < 0) {
          throw new IllegalStateException("Could not find x axis value: " + threads + 
                                            " / " + rr.getBenchmarkName());
        }
        
        if (seriesData.count[index] == 0) {
          seriesData.count[index]++;
          seriesData.executions[index] = rr.getTotalExecutions();
          return AVERAGE_COUNT == 1 ? dataComplete() : false;
        } else if (seriesData.count[index] < AVERAGE_COUNT) {
          seriesData.count[index]++;
          seriesData.executions[index] += 
              (rr.getTotalExecutions() - seriesData.executions[index]) / seriesData.count[index];
          return seriesData.count[index] == AVERAGE_COUNT ? dataComplete() : false;
        } else {
          return dataComplete();
        }
      }
      
      public void generateGraphs() throws IOException {
        for (String s : CLASSES) {
          SeriesData sd = seriesData.get(s);
          if (! sd.dataComplete()) {
            return;
          }
          
          CategorySeries series = chart.addSeries(sd.name, 
                                                  X_AXIS_VALUES, Arrays.asList(sd.executions));
          series.setMarker(SeriesMarkers.NONE);
        }
        
        chartGenerated(chart, 
                       EXPORT_GRAPHS ? new File(outputFolder, chartName).getAbsolutePath() : null);
      }
      
      private static class SeriesData {
        public final String name;
        public final Integer[] executions = new Integer[X_AXIS_VALUES.size()];
        public final int[] count = new int[executions.length];
        private boolean dataComplete = false;
        
        public SeriesData(String name) {
          this.name = name;
        }
        
        public boolean dataComplete() {
          if (dataComplete) {
            return true;
          }
          for (int i = 0; i < count.length; i++) {
            if (count[i] < AVERAGE_COUNT) {
              return false;
            }
          }
          return dataComplete = true;
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

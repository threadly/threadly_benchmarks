package org.threadly.concurrent.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.knowm.xchart.Chart;
import org.knowm.xchart.Series;
import org.knowm.xchart.SeriesMarker;
import org.knowm.xchart.StyleManager.ChartTheme;
import org.knowm.xchart.StyleManager.ChartType;
import org.knowm.xchart.StyleManager.LegendPosition;
import org.knowm.xchart.StyleManager.TextAlignment;
import org.knowm.xchart.SwingWrapper;
import org.skife.jdbi.v2.DBI;
import org.threadly.concurrent.benchmark.dao.BenchmarkDao;
import org.threadly.concurrent.benchmark.dao.RunRecord;
import org.threadly.util.StringUtils;

public class DatabaseGraphPngExport {
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
    } else if (args[5].equalsIgnoreCase("OverTime")) {
      new ScehdulerPerformanceOverTime("release").generateGraphs(dao, outputFolder);
    } else {
      System.err.println("Unknown generation mode: " + args[5]);
      printUsage();
      System.exit(-4);
    }
  }
  
  private static void printUsage() {
    System.err.println("java -cp <classpath> " + BenchmarkCollectionRunner.class + 
                         " <dbHost> <dbUser> <dbPass> <dbName> <outputDirectory> <ByThreads|OverTime>");
  }
  
  private static interface GraphGenerator {
    public void generateGraphs(BenchmarkDao dao, File outputFolder);
  }
  
  private static class ScehdulerPerformanceByThreads implements GraphGenerator {
    @Override
    public void generateGraphs(BenchmarkDao dao, File outputFolder) {
      // TODO Auto-generated method stub
      
    }
  }
  
  private static class ScehdulerPerformanceOverTime implements GraphGenerator {
    private final String branchPrefix;
    
    public ScehdulerPerformanceOverTime(String branchPrefix) {
      this.branchPrefix = branchPrefix;
    }
    
    @Override
    public void generateGraphs(BenchmarkDao dao, File outputFolder) {
      int maxClassGroupId = dao.getMaxClassGroupId();
      for (int cg = 0; cg < maxClassGroupId; cg++) {
        int minBenchmarkGroupId = dao.getMinBenchmarkGroupId(cg);
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
          
          Chart chart = new Chart(1024, 768, ChartTheme.GGPlot2);    
          chart.setChartTitle(identifier);   
          chart.setXAxisTitle("Version");    
          chart.setYAxisTitle("Executions");   
          chart.getStyleManager().setLegendPosition(LegendPosition.OutsideE);
          chart.getStyleManager().setXAxisLabelRotation(270);
          if (branchPrefix.startsWith("release")) {
            chart.getStyleManager().setChartType(ChartType.Bar);
            chart.getStyleManager().setBarsOverlapped(true);
            chart.getStyleManager().setBarWidthPercentage(1);
          } else {
            chart.getStyleManager().setChartType(ChartType.Line);
          }
          
          for (String name : benchmarkNames) {
            List<RunRecord> individualResults = new ArrayList<RunRecord>(results.size() / 8);
            List<Object> releases = new ArrayList<Object>(results.size() / 8);
            List<Integer> executions = new ArrayList<Integer>(results.size() / 8);
            for (RunRecord rr : results) {
              if (rr.getBenchmarkName().equals(name) && 
                  rr.getBranchName().startsWith(branchPrefix)) {
                individualResults.add(rr);
                
                Object xIdentifier;
                if (branchPrefix.startsWith("release")) {
                  /*Pattern p = Pattern.compile("[0-9]+\\.[0-9]");
                  Matcher m = p.matcher(rr.getBranchName());
                  if (! m.find()) {
                    throw new IllegalStateException("Unable to parse release: " + rr.getBranchName());
                  }
                  xIdentifier = Double.parseDouble(m.group());*/
                  xIdentifier = rr.getBranchName();
                } else {
                  xIdentifier = new Date(rr.getRunTimestamp());
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
            
            Series series = chart.addSeries(name, releases, executions);
            series.setMarker(SeriesMarker.NONE);
          }
          
          chart.getStyleManager().setYAxisLabelAlignment(TextAlignment.Right);    
          chart.getStyleManager().setPlotPadding(0);
          chart.getStyleManager().setAxisTickSpacePercentage(.95);
          
          // TODO - remove and instead write to image
          new SwingWrapper(chart).displayChart();
          try {
            Thread.sleep(1000 * 60);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }
}

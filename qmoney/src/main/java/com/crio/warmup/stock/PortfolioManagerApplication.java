package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;

import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;

//import com.crio.warmup.stock.dto.PortfolioTrade;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
//import java.nio.file.Files;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;

import org.apache.commons.math3.util.FastMath;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

//build successful in terminal, checkStyleMain error in assessment??!!!, TRIAL 2

public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Read the json file provided in the argument[0]. The file will be avilable in
  // the classpath.
  // 1. Use #resolveFileFromResources to get actual file from classpath.
  // 2. parse the json file using ObjectMapper provided with #getObjectMapper,
  // and extract symbols provided in every trade.
  // return the list of all symbols in the same order as provided in json.
  // Test the function using gradle commands below
  // ./gradlew run --args="trades.json"
  // Make sure that it prints below String on the console -
  // ["AAPL","MSFT","GOOGL"]
  // Now, run
  // ./gradlew build and make sure that the build passes successfully
  // There can be few unused imports, you will need to fix them to make the build
  // pass.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);

    // byte[] byteArray = Files.readAllBytes(file.toPath());
    // String content = new String(byteArray);

    ObjectMapper mapper = getObjectMapper();
    PortfolioTrade[] trades = mapper.readValue(file, PortfolioTrade[].class);

    List<String> symbols = new ArrayList<>();
    for (int i = 0; i < trades.length; i++) {
      symbols.add(trades[i].getSymbol());
    }

    // System.out.println(symbols);

    return symbols;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread()
    .getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/kevalya-rajvi-ME_QMONEY/"
        + "qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@6d9f7a80";
    String functionNameFromTestFileInStackTrace = "mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "22";

    return Arrays.asList(new String[] { valueOfArgument0, 
      resultOfResolveFilePathArgs0, toStringOfObjectMapper,
      functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objectmapper = getObjectMapper();
    File newfile = resolveFileFromResources(args[0]);
    PortfolioTrade[] module1 = objectmapper.readValue(newfile, PortfolioTrade[].class);
    List<String> list = new ArrayList<>();

    if (module1 == null) {
      return Collections.emptyList();
    } else {
      for (int i = 0; i < module1.length; i++) {
        list.add(module1[i].getSymbol());
      }
    }

    LocalDate newdate = LocalDate.parse(args[1]);
    for (int r = 0; r < list.size(); r++) {
      if (newdate.compareTo((module1[r].getPurchaseDate())) < 0) {
        throw new RuntimeException();
      }

    }

    List<TotalReturnsDto> newlist = new ArrayList<>();
    for (int index = 0; index < list.size(); index++) {

      RestTemplate restTemplate = new RestTemplate();
      TiingoCandle[] result = restTemplate.getForObject(
          "https://api.tiingo.com/tiingo/daily/{ticker}/prices?"
          + "startDate={startdate}&endDate={enddate}&token={token}",
          TiingoCandle[].class, module1[index].getSymbol(),
           module1[index].getPurchaseDate(), args[1],
          "c20ff9c98c36c01588ed641ace2bd65ec932cff3");
      if (result != null) {

        for (int j = 0; j < result.length; j++) {
          if (j == result.length - 1) {
            newlist.add(new TotalReturnsDto(module1[index].getSymbol(), result[j].getClose()));
          }
        }
      } else {
        throw new RuntimeException();
      }
    }

    Collections.sort(newlist, new Comparator<TotalReturnsDto>() {

      @Override
      public int compare(TotalReturnsDto o1, TotalReturnsDto o2) {
        return o1.getClosingPrice().compareTo(o2.getClosingPrice());
      }

    });
    List<String> sortedlist = new ArrayList<>();
    for (int p = 0; p < list.size(); p++) {
      sortedlist.add(newlist.get(p).getSymbol());
    }
    Collections.sort(newlist, new Comparator<TotalReturnsDto>() {

      @Override
      public int compare(TotalReturnsDto o1, TotalReturnsDto o2) {
        return o1.getClosingPrice().compareTo(o2.getClosingPrice());
      }

    });
    return sortedlist;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) 
      throws IOException, URISyntaxException {
    ObjectMapper objectmapper = getObjectMapper();
    File newfile = resolveFileFromResources(args[0]);
    PortfolioTrade[] module1 = objectmapper.readValue(newfile, PortfolioTrade[].class);
    List<String> list = new ArrayList<>();

    if (module1 == null) {
      return Collections.emptyList();
    } else {
      for (int i = 0; i < module1.length; i++) {
        list.add(module1[i].getSymbol());
      }
    }

    LocalDate newdate = LocalDate.parse(args[1]);
    for (int r = 0; r < list.size(); r++) {
      if (newdate.compareTo((module1[r].getPurchaseDate())) < 0) {
        throw new RuntimeException();
      }

    }

    List<AnnualizedReturn> annual = new ArrayList<>();
    for (int index = 0; index < list.size(); index++) {

      RestTemplate restTemplate = new RestTemplate();
      TiingoCandle[] result = restTemplate.getForObject(
          "https://api.tiingo.com/tiingo/daily/{ticker}/prices?startDate={startdate}&endDate={enddate}&token={token}",
          TiingoCandle[].class, module1[index].getSymbol(), 
          module1[index].getPurchaseDate(), args[1],
          "c20ff9c98c36c01588ed641ace2bd65ec932cff3");
      if (result != null) {

        Double open = result[0].getOpen();
        Double close = result[result.length - 1].getClose();
        annual.add(calculateAnnualizedReturns(newdate, module1[index], open, close));

      } else {
        throw new RuntimeException();
      }

    }
    Collections.sort(annual, new Comparator<AnnualizedReturn>() {

      @Override
      public int compare(AnnualizedReturn o1, AnnualizedReturn o2) {
        return o2.getAnnualizedReturn().compareTo(o1.getAnnualizedReturn());
      }

    });
    return annual;

  }

  public static AnnualizedReturn calculateAnnualizedReturns(
      LocalDate endDate, PortfolioTrade trade, Double buyPrice,
      Double sellPrice) {
    Double totalReturns = (sellPrice - buyPrice) / buyPrice;
    LocalDate purchasedate = trade.getPurchaseDate();
    Double days = (double) ChronoUnit.DAYS.between(purchasedate, endDate);
    
    Double y = days / 365;
    Double annualizedReturns = FastMath.pow(1 + totalReturns, 1 / y) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualizedReturns, totalReturns);
    
  }

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
    
    RestTemplate restTemplate = new RestTemplate();
    PortfolioManagerFactory factory = new PortfolioManagerFactory();
    PortfolioManager portfolioManager = factory.getPortfolioManager(restTemplate);
    // String file = args[0];
    ObjectMapper objectmapper = getObjectMapper();
    File newfile = resolveFileFromResources(args[0]);
    PortfolioTrade[] portfolioTrades = objectmapper.readValue(newfile, PortfolioTrade[].class);
    
    LocalDate endDate = LocalDate.parse(args[1]);
    // String contents = readFileAsString(file);
    return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
    
  }

  public static void main(String[] args) throws Exception {
   
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));

  }

}
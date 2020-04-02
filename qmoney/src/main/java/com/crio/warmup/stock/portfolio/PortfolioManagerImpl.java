package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {



  public RestTemplate restTemplate;
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  



  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  //CHECKSTYLE:OFF





  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
        LocalDate endDate) throws JsonProcessingException {
    
  List<AnnualizedReturn> annual = new ArrayList<>();
  for (int index = 0; index < portfolioTrades.size(); index++) {

     List<Candle> newlist = getStockQuote(portfolioTrades.get(index).getSymbol(), portfolioTrades.get(index).getPurchaseDate(), endDate);
    if (newlist != null) {

      Double open = newlist.get(0).getOpen();
      Double close = newlist.get(newlist.size() - 1).getClose();
      Double totalReturns = (close - open) / open;
      LocalDate purchasedate = portfolioTrades.get(index).getPurchaseDate();
      Double days = (double) ChronoUnit.DAYS.between(purchasedate, endDate);
    
      Double y = days / 365;
      Double annualizedReturns = FastMath.pow(1 + totalReturns, 1 / y) - 1;
      annual.add(new AnnualizedReturn(portfolioTrades.get(index).getSymbol(), annualizedReturns, totalReturns));

    } else {
      throw new RuntimeException();
    }
      
  }
    Collections.sort(annual, getComparator());
    return annual;

    }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo thirdparty APIs to a separate function.
  //  It should be split into fto parts.
  //  Part#1 - Prepare the Url to call Tiingo based on a template constant,
  //  by replacing the placeholders.
  //  Constant should look like
  //  https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=?&endDate=?&token=?
  //  Where ? are replaced with something similar to <ticker> and then actual url produced by
  //  replacing the placeholders with actual parameters.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
    String url = buildUri(symbol, from, to);
    ObjectMapper mapper = new ObjectMapper();
    String result = restTemplate.getForObject(url, String.class);
    Candle[] abc = mapper.readValue(result, TiingoCandle[].class);
    List<Candle> newresult = Arrays.asList(abc);   
    return newresult;
    
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate=" 
          + startDate + "&endDate=" + endDate + "&token=38279ba8f3e5e2679d83bf9ed962a63a809aa69c";
    return uriTemplate;
  }



}
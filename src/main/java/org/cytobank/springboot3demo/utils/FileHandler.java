package org.cytobank.springboot3demo.utils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.cytobank.io.DoubleFile;
import org.cytobank.io.LargeFile;
import org.cytobank.springboot3demo.model.Input;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

@Log
public class FileHandler implements RequestHandler<Input, String> {

  private static final boolean LOG_ENABLED;
  private static final int READ_TIMES;

  static {
    String logEnabledStr = System.getenv().get("logEnabled");
    if (StringUtils.isBlank(logEnabledStr)) {
      LOG_ENABLED = true;
    } else {
      LOG_ENABLED = Boolean.parseBoolean(logEnabledStr);
    }

    String readTimesStr = System.getenv().get("readTimes");
    if (StringUtils.isBlank(logEnabledStr)) {
      READ_TIMES = 1;
    } else {
      READ_TIMES = Integer.parseInt(readTimesStr);
    }
  }

  private void checkAndPrint(String message) {
    if (LOG_ENABLED) {
      print(message);
    }
  }

  private void print(String message) {
    log.info(message);
  }

  @Override
  public String handleRequest(Input path, Context context) {
    for (int i = 0; i < READ_TIMES; i++) {
      readFile(path.getPath());
    }
    return null;
  }

  public double[] loadChannelStripeFileToCache(String path) {
    return loadFromFile(path);
  }

  private void readFile(String path) {
    Instant start = Instant.now();
    try (FileChannel fileChannel = new FileInputStream(Paths.get(path).toFile()).getChannel()) {
      DoubleFile doubleFile = new DoubleFile(fileChannel, LargeFile.READ_ONLY);
      long size = fileChannel.size();
      long eventsNumber = size / DoubleFile.BYTES_PER_DOUBLE;
      checkAndPrint(String.format("events number: %d", eventsNumber));
      for (int i = 0; i < eventsNumber; i++) {
        checkAndPrint(String.format("events[%d] = %f", i, doubleFile.get(i)));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    print(String.format("Used: %dms", Duration.between(start, Instant.now()).toMillis()));
  }

  private double[] loadFromFile(String stripeFilePath) {
    Instant start = Instant.now();
    double[] events;

    File eventFile = Path.of(stripeFilePath).toFile();
    // calculate number of raw events
    int numberOfRawEvents = (int) eventFile.length() / (Double.SIZE / Byte.SIZE);

    log.info("numberOfRawEvents: " + numberOfRawEvents);

    events = new double[numberOfRawEvents];
    try (FileChannel fileChannel = new FileInputStream(eventFile).getChannel()) {
      DoubleFile doubleFile = new DoubleFile(fileChannel, LargeFile.READ_ONLY);
      doubleFile.get(events);
      // put events to cache
//      LOCAL_CACHE.put(getEventsCacheKey(), events);
    } catch (IOException e) {
      e.printStackTrace();
    }

    print(String.format("Load the whole stripe file used: %dms", Duration.between(start, Instant.now()).toMillis()));

    return events;
  }
}

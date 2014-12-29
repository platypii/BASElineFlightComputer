package com.platypii.baseline.data;

import java.util.Locale;


// A class to track mean and variance
public class Stat {

  long n = 0;
  private double total = 0.0;
  private double mean = 0.0;
  private double M2 = 0.0;


  public void addSample(double x) {
    // Online mean and variance (thanks Knuth)
    n++;
    total += x;
    double delta = x - mean;
    mean = mean + delta / n;
    M2 = M2 + delta * (x - mean);
  }

  public double total() {
    return total;
  }

  public double mean() {
    return mean;
  }

  public double var() {
    // Sample Variance
    return M2 / n;

    // Population Variance
//    return M2 / (n - 1);
  }

  public String toString() {
    return String.format(Locale.US, "%.3f ± %.3f", mean, var());
//      return mean + " ± " + var();
  }

}

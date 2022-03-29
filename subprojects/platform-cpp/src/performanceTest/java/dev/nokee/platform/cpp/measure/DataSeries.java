/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.cpp.measure;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of measurements of some given units.
 */
public class DataSeries<Q> implements Iterable<Amount<Q>> {
	private final List<Amount<Q>> values = new ArrayList<>();
    private final Amount<Q> average;
    private final Amount<Q> median;
    private final Amount<Q> max;
    private final Amount<Q> min;
    // https://en.wikipedia.org/wiki/Standard_error
    private final Amount<Q> standardError;

    public DataSeries(Iterable<? extends Amount<Q>> values) {
        for (Amount<Q> value : values) {
            if (value != null) {
                this.values.add(value);
            }
        }

        if (this.values.isEmpty()) {
            average = null;
            median = null;
            max = null;
            min = null;
            standardError = null;
            return;
        }

        Amount<Q> total = this.values.get(0);
        Amount<Q> min = this.values.get(0);
        Amount<Q> max = this.values.get(0);
        for (int i = 1; i < this.values.size(); i++) {
            Amount<Q> amount = this.values.get(i);
            total = total.plus(amount);
            min = min.compareTo(amount) <= 0 ? min : amount;
            max = max.compareTo(amount) >= 0 ? max : amount;
        }
        List<Amount<Q>> sorted = new ArrayList<>(this.values);
        Collections.sort(sorted);
        Amount<Q> medianLeft = sorted.get((sorted.size() - 1) / 2);
        Amount<Q> medianRight = sorted.get((sorted.size() - 1) / 2 + 1 - sorted.size() % 2);
        median = medianLeft.plus(medianRight).div(2);
        average = total.div(this.values.size());
        this.min = min;
        this.max = max;

        BigDecimal sumSquares = BigDecimal.ZERO;
        Units<Q> baseUnits = average.getUnits().getBaseUnits();
        BigDecimal averageValue = average.toUnits(baseUnits).getValue();
        for (Amount<Q> amount : this) {
            BigDecimal diff = amount.toUnits(baseUnits).getValue();
            diff = diff.subtract(averageValue);
            diff = diff.multiply(diff);
            sumSquares = sumSquares.add(diff);
        }
        // This isn't quite right, as we may lose precision when converting to a double
        BigDecimal result = BigDecimal.valueOf(Math.sqrt(sumSquares.divide(BigDecimal.valueOf(this.values.size()), RoundingMode.HALF_UP).doubleValue())).setScale(2, RoundingMode.HALF_UP);

        standardError = Amount.valueOf(result, baseUnits);
    }

    public Amount<Q> getAverage() {
        return average;
    }

    public Amount<Q> getMedian() {
        return median;
    }

    public Amount<Q> getMin() {
        return min;
    }

    public Amount<Q> getMax() {
        return max;
    }

    public Amount<Q> getStandardError() {
        return standardError;
    }

    public static double confidenceInDifference(DataSeries<?> first, DataSeries<?> second) {
//        return 1 - new MannWhitneyUTest().mannWhitneyUTest(first.asDoubleArray(), second.asDoubleArray());
		throw new UnsupportedOperationException();
    }

    public List<Double> asDoubleList() {
        return values.stream().map(Amount::getValue).map(BigDecimal::doubleValue).collect(Collectors.toList());
    }

    private double[] asDoubleArray() {
        return values.stream().map(Amount::getValue).mapToDouble(BigDecimal::doubleValue).toArray();
    }

	@Override
	public Iterator<Amount<Q>> iterator() {
		return values.iterator();
	}

	public Stream<Amount<Q>> stream() {
		return values.stream();
	}

	public static String speedStats(DataSeries<?> measurement) {
		final StringBuilder builder = new StringBuilder();
		builder.append("  ");
		builder.append("median: ").append(measurement.getMedian().format()).append(" ");
		builder.append("min: ").append(measurement.getMin().format()).append(" ");
		builder.append("max: ").append(measurement.getMax().format()).append(" ");
		builder.append("se: ").append(measurement.getStandardError().format()).append(System.lineSeparator());
		builder.append("  > ").append(measurement.stream().map(Amount::format).collect(Collectors.toList()));
		builder.append(System.lineSeparator());
		return builder.toString();
	}
}

package com.example.smartbasketapp;

import java.io.Serializable;
import java.util.List;

public final class BonusCalculator {

    public enum Level {
        BASIC(0, 1),
        SILVER(200, 2),
        GOLD(500, 3);

        public final int threshold;
        public final int percent;

        Level(int threshold, int percent) {
            this.threshold = threshold;
            this.percent = percent;
        }

        public Level next() {
            int i = ordinal() + 1;
            return i < values().length ? values()[i] : null;
        }

        public static Level forTotalSpent(double totalSpent) {
            Level result = BASIC;
            for (Level level : values()) {
                if (totalSpent >= level.threshold) result = level;
            }
            return result;
        }
    }

    public static final class Result implements Serializable {
        private static final long serialVersionUID = 1L;

        public final double points;
        public final double totalSpent;
        public final Level level;

        Result(double points, double totalSpent, Level level) {
            this.points = points;
            this.totalSpent = totalSpent;
            this.level = level;
        }

        public double amountToNextLevel() {
            Level next = level.next();
            return next == null ? 0 : Math.max(0, next.threshold - totalSpent);
        }

        public int progressPercent() {
            Level next = level.next();
            if (next == null) return 100;
            double span = next.threshold - level.threshold;
            double done = totalSpent - level.threshold;
            return (int) Math.max(0, Math.min(100, Math.floor(done / span * 100)));
        }
    }

    private BonusCalculator() {}

    public static Result calculate(List<Double> purchaseTotals) {
        long spentKopecks = 0;
        long pointsKopecks = 0;

        if (purchaseTotals != null) {
            for (Double total : purchaseTotals) {
                if (total == null || total <= 0 || total.isNaN() || total.isInfinite()) continue;
                long kopecks = Math.round(total * 100);
                Level levelBefore = Level.forTotalSpent(spentKopecks / 100.0);
                pointsKopecks += kopecks * levelBefore.percent / 100;
                spentKopecks += kopecks;
            }
        }

        double totalSpent = spentKopecks / 100.0;
        return new Result(pointsKopecks / 100.0, totalSpent, Level.forTotalSpent(totalSpent));
    }
}

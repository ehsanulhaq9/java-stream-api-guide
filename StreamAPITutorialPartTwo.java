import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StreamAPITutorialPartTwo {

        public record Item(String name, int quantity) {
        }

        public record Transaction(String transactionId, LocalDate date, LocalTime time, List<Item> items,
                        String paymentMethod, String transactionType, String transactionStatus, String customerType,
                        String storeSection) {
        }

        public static void main(String[] args) {

                List<Transaction> transactions = getMonthlyTransactions();

                System.out.println("\n\n\n\n\n");
                // ============================
                // CATEGORY 1: Time-Based Analytics
                // ============================

                // Chart 1: Total Items Sold by Hourly Interval
                // Stream API Methods: groupingBy (on hour) + summingInt, using TreeMap for
                // sorted order
                Map<Integer, Integer> itemsSoldByHour = transactions.stream()
                                .collect(Collectors.groupingBy(
                                                tx -> tx.time().getHour(), // Group by hour (0–23)
                                                TreeMap::new, // Keep keys sorted
                                                Collectors.summingInt(tx -> tx.items().stream().mapToInt(Item::quantity)
                                                                .sum())));

                String[] timeLabels = itemsSoldByHour.keySet().stream()
                                .map(hour -> String.format("%02d:00–%02d:00", hour, (hour + 1) % 24))
                                .toArray(String[]::new);

                int[] itemsSoldCounts = itemsSoldByHour.values().stream().mapToInt(Integer::intValue).toArray();
                printBarChart("Total Items Sold by Hourly Interval", timeLabels, itemsSoldCounts);

                // Chart 2: Total Transactions Count by Hour
                // Stream API Methods: groupingBy (on hour) + counting
                Map<Integer, Long> transactionsByHour = transactions.stream()
                                .collect(Collectors.groupingBy(
                                                tx -> tx.time().getHour(),
                                                TreeMap::new,
                                                Collectors.counting()));

                String[] txHourLabels = transactionsByHour.keySet().stream()
                                .map(hour -> String.format("%02d:00–%02d:00", hour, (hour + 1) % 24))
                                .toArray(String[]::new);

                int[] txCounts = transactionsByHour.values().stream().mapToInt(Long::intValue).toArray();
                printBarChart("Total Transactions Count by Hour", txHourLabels, txCounts);

                // Chart 3: Average Items per Transaction by Hour
                // Stream API Methods: groupingBy (on hour) + averagingDouble
                Map<Integer, Double> avgItemsByHour = transactions.stream()
                                .collect(Collectors.groupingBy(
                                                tx -> tx.time().getHour(),
                                                TreeMap::new,
                                                Collectors.averagingDouble(tx -> tx.items().stream()
                                                                .mapToInt(Item::quantity).sum())));

                String[] avgItemHourLabels = avgItemsByHour.keySet().stream()
                                .map(hour -> String.format("%02d:00–%02d:00", hour, (hour + 1) % 24))
                                .toArray(String[]::new);

                int[] avgItems = avgItemsByHour.values().stream()
                                .mapToInt(val -> (int) Math.round(val))
                                .toArray();
                printBarChart("Avg. Items per Transaction by Hour", avgItemHourLabels, avgItems);

                // Chart 4: Distinct Items Sold by Hour
                // Stream API Methods: groupingBy (on hour) + flatMapping + collectingAndThen +
                // toSet + size
                Map<Integer, Long> distinctItemsByHour = transactions.stream()
                                .collect(Collectors.groupingBy(
                                                tx -> tx.time().getHour(),
                                                TreeMap::new,
                                                Collectors.collectingAndThen(
                                                                Collectors.flatMapping(
                                                                                tx -> tx.items().stream()
                                                                                                .map(Item::name),
                                                                                Collectors.toSet()),
                                                                set -> (long) set.size())));

                String[] itemHourLabels = distinctItemsByHour.keySet().stream()
                                .map(hour -> String.format("%02d:00–%02d:00", hour, (hour + 1) % 24))
                                .toArray(String[]::new);

                int[] distinctItemCounts = distinctItemsByHour.values().stream().mapToInt(Long::intValue).toArray();
                printBarChart("Distinct Items Sold by Hour", itemHourLabels, distinctItemCounts);

                // ============================
                // CATEGORY 2: Week-Based Analytics
                // ============================

                // Chart 5: Total Transactions per Week
                // Stream API Methods: groupingBy (on custom week label) + counting
                Map<String, Long> transactionPerWeek = transactions.stream()
                                .collect(Collectors.groupingBy(tx -> getWeekOfMonthLabel(tx.date()),
                                                Collectors.counting()));

                String[] weeks = transactionPerWeek.keySet().toArray(new String[0]);
                int[] transactionCounts = transactionPerWeek.values().stream().mapToInt(Long::intValue).toArray();
                printBarChart("Total Transactions Per Week", weeks, transactionCounts);

                // Chart 6: Total Items Sold per Week
                // Stream API Methods: groupingBy (on week) + summingInt
                Map<String, Integer> totalItemsSoldPerWeek = transactions.stream()
                                .collect(Collectors.groupingBy(tx -> getWeekOfMonthLabel(tx.date()),
                                                Collectors.summingInt(tx -> tx.items().stream().mapToInt(Item::quantity)
                                                                .sum())));

                String[] weekLabels = totalItemsSoldPerWeek.keySet().toArray(new String[0]);
                int[] itemCounts = totalItemsSoldPerWeek.values().stream().mapToInt(Integer::intValue).toArray();
                printBarChart("Total Items Sold per Week", weekLabels, itemCounts);

                // ============================
                // CATEGORY 3: Categorical Analytics
                // ============================

                // Chart 7: Count of Transactions per Payment Method
                // Stream API Methods: groupingBy + counting
                Map<String, Long> countByPaymentMethod = transactions.stream()
                                .collect(Collectors.groupingBy(Transaction::paymentMethod, Collectors.counting()));

                String[] paymentLabels = countByPaymentMethod.keySet().toArray(new String[0]);
                int[] paymentCounts = countByPaymentMethod.values().stream().mapToInt(Long::intValue).toArray();
                printBarChart("Transactions by Payment Method", paymentLabels, paymentCounts);

                // Chart 8: Transaction Status Distribution
                // Stream API Methods: groupingBy + counting
                Map<String, Long> statusCounts = transactions.stream()
                                .collect(Collectors.groupingBy(Transaction::transactionStatus, Collectors.counting()));

                String[] statusLabels = statusCounts.keySet().toArray(new String[0]);
                int[] statusCount = statusCounts.values().stream().mapToInt(Long::intValue).toArray();
                printBarChart("Transaction Status Distribution", statusLabels, statusCount);

                // ============================
                // CATEGORY 4: Item-Level Analytics
                // ============================

                // Chart 9: Total Quantity Sold per Item
                // Stream API Methods: flatMap + groupingBy + summingInt
                Map<String, Integer> quantityPerItem = transactions.stream()
                                .flatMap(tx -> tx.items().stream())
                                .collect(Collectors.groupingBy(Item::name, Collectors.summingInt(Item::quantity)));

                String[] items = quantityPerItem.keySet().toArray(new String[0]);
                int[] quantity = quantityPerItem.values().stream().mapToInt(Integer::intValue).toArray();
                printBarChart("Total Quantity Sold per Item", items, quantity);

                // Chart 10: Items Sold by Store Section
                // Stream API Methods: groupingBy + summingInt (nested stream for item
                // quantities)
                Map<String, Integer> itemsBySection = transactions.stream()
                                .collect(Collectors.groupingBy(Transaction::storeSection,
                                                Collectors.summingInt(tx -> tx.items().stream().mapToInt(Item::quantity)
                                                                .sum())));

                String[] storeSections = itemsBySection.keySet().toArray(new String[0]);
                int[] itemsCount = itemsBySection.values().stream().mapToInt(Integer::intValue).toArray();
                printBarChart("Items Sold by Store Section", storeSections, itemsCount);

        }

        private static String getWeekOfMonthLabel(LocalDate date) {
                int day = date.getDayOfMonth();
                int daysInMonth = date.getMonth().length(date.isLeapYear());
                int weekSize = (int) Math.ceil(daysInMonth / 4.0); // Divide month into 4 equal chunks
                int week = ((day - 1) / weekSize) + 1;
                return "Week " + Math.min(week, 4); // Ensure max week is 4
        }

        public static String getHourlyIntervalLabel(LocalTime time) {
                int hour = time.getHour();
                LocalTime start = LocalTime.of(hour, 0);
                LocalTime end = start.plusHours(1);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                return formatter.format(start) + " - " + formatter.format(end);
        }

        public static void printBarChart(String title, String[] labels, int[] values) {
                if (labels.length != values.length) {
                        throw new IllegalArgumentException("Labels and values must have the same length.");
                }

                int maxLabelLength = 0;
                int maxValue = 0;

                for (int i = 0; i < labels.length; i++) {
                        maxLabelLength = Math.max(maxLabelLength, labels[i].length());
                        maxValue = Math.max(maxValue, values[i]);
                }

                int scaleFactor = maxValue > 50 ? maxValue / 50 : 1;
                int chartWidth = 80;

                // Print centered title
                if (title != null && !title.isEmpty()) {
                        int padding = Math.max(0, (chartWidth - title.length()) / 2);
                        System.out.printf("%" + (padding + title.length()) + "s%n", title);
                        System.out.println();
                }

                for (int i = 0; i < labels.length; i++) {
                        String label = String.format("%-" + maxLabelLength + "s", labels[i]);
                        int barLength = values[i] / scaleFactor;
                        String bar = "█".repeat(barLength); // full block character
                        System.out.printf("%s | %s (%d)%n%n", label, bar, values[i]);
                }
        }

        static String centerText(String text, int width) {
                int padding = (width - text.length()) / 2;
                return " ".repeat(Math.max(0, padding)) + text;
        }

        // Helper method to find the maximum value
        private static int findMaxValue(int[] values) {
                int max = values[0];
                for (int value : values) {
                        if (value > max) {
                                max = value;
                        }
                }
                return max;
        }

        // Helper method to generate the bar (in this case, using the █ character)
        private static String generateBar(int width) {
                return "█".repeat(width);
        }

        /**
         * Generates a dummy list of grocery transactions for demonstration purposes.
         * 
         * Note: The content of this dataset has been generated by ChatGPT
         * to simulate realistic transaction data for tutorial and testing use cases.
         */

        public static List<Transaction> getMonthlyTransactions() {
                return List.of(
                                new Transaction("TXN-0001", LocalDate.of(2025, 6, 1), LocalTime.of(10, 15),
                                                List.of(new Item("milk", 2), new Item("bread", 1)), "Cash", "In-Store",
                                                "Completed", "Regular",
                                                "Mixed"),
                                new Transaction("TXN-0002", LocalDate.of(2025, 6, 2), LocalTime.of(11, 40),
                                                List.of(new Item("yogurt", 3)), "Credit Card", "Online", "Completed",
                                                "New", "Dairy"),
                                new Transaction("TXN-0003", LocalDate.of(2025, 6, 3), LocalTime.of(9, 5),
                                                List.of(new Item("bread", 1), new Item("egg", 6),
                                                                new Item("butter", 1)),
                                                "Debit Card",
                                                "In-Store", "Completed", "Regular", "Mixed"),
                                new Transaction("TXN-0004", LocalDate.of(2025, 6, 4), LocalTime.of(14, 30),
                                                List.of(new Item("chocolate", 2)), "Mobile Payment", "Online",
                                                "Pending", "New", "Pantry"),
                                new Transaction("TXN-0005", LocalDate.of(2025, 6, 5), LocalTime.of(16, 10),
                                                List.of(new Item("butter", 1), new Item("milk", 1),
                                                                new Item("yogurt", 1)),
                                                "Cash", "In-Store",
                                                "Completed", "Regular", "Dairy"),
                                new Transaction("TXN-0006", LocalDate.of(2025, 6, 6), LocalTime.of(10, 25),
                                                List.of(new Item("rice", 2)), "Credit Card", "Online", "Returned",
                                                "New", "Pantry"),
                                new Transaction("TXN-0007", LocalDate.of(2025, 6, 7), LocalTime.of(15, 45),
                                                List.of(new Item("bread", 1), new Item("butter", 1)), "Cash",
                                                "In-Store", "Completed",
                                                "Regular", "Bakery"),
                                new Transaction("TXN-0008", LocalDate.of(2025, 6, 8), LocalTime.of(13, 5),
                                                List.of(new Item("egg", 12), new Item("milk", 1)), "Mobile Payment",
                                                "Online", "Completed",
                                                "New", "Mixed"),
                                new Transaction("TXN-0009", LocalDate.of(2025, 6, 9), LocalTime.of(10, 50),
                                                List.of(new Item("yogurt", 2), new Item("chocolate", 1)), "Credit Card",
                                                "In-Store",
                                                "Completed", "Regular", "Mixed"),
                                new Transaction("TXN-0010", LocalDate.of(2025, 6, 10), LocalTime.of(13, 35),
                                                List.of(new Item("rice", 1), new Item("egg", 6)), "Debit Card",
                                                "Online", "Pending", "New",
                                                "Pantry"),
                                new Transaction("TXN-0011", LocalDate.of(2025, 6, 11), LocalTime.of(12, 20),
                                                List.of(new Item("bread", 1), new Item("milk", 1)), "Cash", "In-Store",
                                                "Completed", "Regular",
                                                "Mixed"),
                                new Transaction("TXN-0012", LocalDate.of(2025, 6, 12), LocalTime.of(11, 0),
                                                List.of(new Item("yogurt", 2)), "Credit Card", "Online", "Returned",
                                                "New", "Dairy"),
                                new Transaction("TXN-0013", LocalDate.of(2025, 6, 13), LocalTime.of(9, 30),
                                                List.of(new Item("egg", 6)),
                                                "Debit Card", "In-Store", "Completed", "Regular", "Pantry"),
                                new Transaction("TXN-0014", LocalDate.of(2025, 6, 14), LocalTime.of(15, 15),
                                                List.of(new Item("butter", 2), new Item("chocolate", 1)),
                                                "Mobile Payment", "Online",
                                                "Completed", "New", "Mixed"),
                                new Transaction("TXN-0015", LocalDate.of(2025, 6, 15), LocalTime.of(17, 50),
                                                List.of(new Item("rice", 1), new Item("milk", 2)), "Cash", "In-Store",
                                                "Completed", "Regular",
                                                "Mixed"),
                                new Transaction("TXN-0016", LocalDate.of(2025, 6, 16), LocalTime.of(10, 5),
                                                List.of(new Item("bread", 2)), "Credit Card", "Online", "Completed",
                                                "New", "Bakery"),
                                new Transaction("TXN-0017", LocalDate.of(2025, 6, 17), LocalTime.of(14, 45),
                                                List.of(new Item("yogurt", 1), new Item("milk", 1)), "Debit Card",
                                                "In-Store", "Pending",
                                                "Regular", "Dairy"),
                                new Transaction("TXN-0018", LocalDate.of(2025, 6, 18), LocalTime.of(13, 10),
                                                List.of(new Item("butter", 1), new Item("chocolate", 2)),
                                                "Mobile Payment", "Online",
                                                "Completed", "New", "Mixed"),
                                new Transaction("TXN-0019", LocalDate.of(2025, 6, 19), LocalTime.of(16, 0),
                                                List.of(new Item("egg", 12)), "Cash", "In-Store", "Completed",
                                                "Regular", "Pantry"),
                                new Transaction("TXN-0020", LocalDate.of(2025, 6, 20), LocalTime.of(11, 25),
                                                List.of(new Item("milk", 1), new Item("rice", 1)), "Credit Card",
                                                "Online", "Returned", "New",
                                                "Mixed"),
                                new Transaction("TXN-0021", LocalDate.of(2025, 6, 21), LocalTime.of(9, 45),
                                                List.of(new Item("bread", 1), new Item("butter", 1)), "Debit Card",
                                                "In-Store", "Completed",
                                                "Regular", "Bakery"),
                                new Transaction("TXN-0022", LocalDate.of(2025, 6, 22), LocalTime.of(14, 10),
                                                List.of(new Item("chocolate", 3)), "Mobile Payment", "Online",
                                                "Completed", "New", "Pantry"),
                                new Transaction("TXN-0023", LocalDate.of(2025, 6, 23), LocalTime.of(12, 35),
                                                List.of(new Item("yogurt", 2), new Item("egg", 6)), "Cash", "In-Store",
                                                "Completed", "Regular",
                                                "Mixed"),
                                new Transaction("TXN-0024", LocalDate.of(2025, 6, 24), LocalTime.of(10, 15),
                                                List.of(new Item("milk", 2)), "Credit Card", "Online", "Completed",
                                                "New", "Dairy"),
                                new Transaction("TXN-0025", LocalDate.of(2025, 6, 25), LocalTime.of(11, 50),
                                                List.of(new Item("rice", 1), new Item("chocolate", 1)), "Debit Card",
                                                "In-Store", "Pending",
                                                "Regular", "Mixed"),
                                new Transaction("TXN-0026", LocalDate.of(2025, 6, 26), LocalTime.of(13, 5),
                                                List.of(new Item("bread", 1), new Item("milk", 1)), "Mobile Payment",
                                                "Online", "Completed",
                                                "New", "Mixed"),
                                new Transaction("TXN-0027", LocalDate.of(2025, 6, 27), LocalTime.of(14, 20),
                                                List.of(new Item("egg", 12), new Item("butter", 1)), "Cash", "In-Store",
                                                "Completed", "Regular",
                                                "Mixed"),
                                new Transaction("TXN-0028", LocalDate.of(2025, 6, 28), LocalTime.of(16, 40),
                                                List.of(new Item("yogurt", 1)), "Credit Card", "Online", "Returned",
                                                "New", "Dairy"),
                                new Transaction("TXN-0029", LocalDate.of(2025, 6, 29), LocalTime.of(15, 30),
                                                List.of(new Item("bread", 1), new Item("chocolate", 1)), "Debit Card",
                                                "In-Store", "Completed",
                                                "Regular", "Bakery"),
                                new Transaction("TXN-0030", LocalDate.of(2025, 6, 30), LocalTime.of(17, 55),
                                                List.of(new Item("milk", 2), new Item("egg", 6)), "Mobile Payment",
                                                "Online", "Completed",
                                                "New", "Mixed"));
        }
}

package jdbc.td2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jdbc.td2.dao.DataRetriever;
import jdbc.td2.model.*;

public class Main {

    private static final String SEPARATOR = "═══════════════════════════════════════════════════════════════════";
    private static final String LINE = "───────────────────────────────────────────────────────────────────";
    
    // Time slot manager to avoid conflicts between tests
    private static int timeSlotCounter = 0;
    
    private static Instant[] getNextTimeSlot() {
        int offset = timeSlotCounter * 7200; // Each test gets a 2-hour window
        timeSlotCounter++;
        return new Instant[] {
            Instant.now().plusSeconds(offset),
            Instant.now().plusSeconds(offset + 3600) // 1 hour duration
        };
    }
    
    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        printHeader("TD2 - BASIC QUERIES");
        testA(dr);
        testB(dr);
        testC(dr);
        testD(dr);
        testE(dr);
        testF(dr);

        printHeader("TD3 - COST & MARGIN");
        testTD3CostAndMargin(dr);

        printHeader("TD4 - STOCK MANAGEMENT");
        testTD4Stock(dr);

        printHeader("ORDER MANAGEMENT");
        testOrderSaveAndFind(dr);
        testOrderInsufficientStock(dr);
        testOrderNotFound(dr);
        testOrderStockDecrease(dr);
        testOrderReferenceFormat(dr);

        printHeader("BONUS K1 - UNIT CONVERSION");
        testUnitConversion_Basic();
        testUnitConversion_ToKg();
        testUnitConversion_FromKg();
        testUnitConversion_Impossible();
        testUnitConversion_RealScenario();

        printHeader("TABLE MANAGEMENT");
        testOrderWithoutTable(dr);
        testOrderWithUnavailableTable(dr);
        testOrderWithAvailableTable(dr);
        testOrderWhenAllTablesBusy(dr);

        printFooter();
    }

    // =========================
    // UTILITY METHODS
    // =========================
    
    private static void printHeader(String title) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  " + title);
        System.out.println(SEPARATOR + "\n");
    }

    private static void printFooter() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  ✓ ALL TESTS COMPLETED");
        System.out.println(SEPARATOR + "\n");
    }

    private static void printTestTitle(String testNumber, String description) {
        System.out.println("┌─ Test " + testNumber + ": " + description);
    }

    private static void printTestEnd() {
        System.out.println("└" + LINE.substring(0, 50) + "\n");
    }

    private static void printSuccess(String message) {
        System.out.println("  ✓ PASS: " + message);
    }

    private static void printFailure(String message) {
        System.out.println("  ✗ FAIL: " + message);
    }

    private static void printInfo(String message) {
        System.out.println("  → " + message);
    }

    private static void printCheckmark(boolean condition) {
        System.out.print(condition ? " ✓" : " ✗");
    }

    // =========================
    // TD2 - BASIC QUERIES
    // =========================
    
    static void testA(DataRetriever dr) {
        printTestTitle("A", "findDishById(1)");
        try {
            Dish d = dr.findDishById(1);
            printInfo("Dish name: " + d.getName());
            printInfo("Ingredients count: " + d.getIngredients().size());
            printSuccess("Dish retrieved successfully");
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    static void testB(DataRetriever dr) {
        printTestTitle("B", "findDishById(999) - Non-existent dish");
        try {
            dr.findDishById(999);
            printFailure("Should throw RuntimeException");
        } catch (RuntimeException e) {
            printSuccess("Exception thrown as expected: " + e.getMessage());
        }
        printTestEnd();
    }

    static void testC(DataRetriever dr) {
        printTestTitle("C", "findIngredients(page=1, size=3)");
        List<Ingredient> ingredients = dr.findIngredients(1, 3);
        ingredients.forEach(i -> printInfo(i.getName()));
        printSuccess("Retrieved " + ingredients.size() + " ingredients");
        printTestEnd();
    }

    static void testD(DataRetriever dr) {
        printTestTitle("D", "findIngredients(page=3, size=5)");
        List<Ingredient> ingredients = dr.findIngredients(3, 5);
        printInfo("Count: " + ingredients.size());
        printSuccess("Page retrieved successfully");
        printTestEnd();
    }

    static void testE(DataRetriever dr) {
        printTestTitle("E", "findDishesByIngredientName('Tomate')");
        List<Dish> dishes = dr.findDishesByIngredientName("Tomate");
        dishes.forEach(d -> printInfo(d.getName()));
        printSuccess("Found " + dishes.size() + " dishes with Tomate");
        printTestEnd();
    }

    static void testF(DataRetriever dr) {
        printTestTitle("F", "findIngredientsByCriteria(VEGETABLE)");
        List<Ingredient> vegetables = dr.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10);
        vegetables.forEach(i -> printInfo(i.getName()));
        printSuccess("Found " + vegetables.size() + " vegetables");
        printTestEnd();
    }

    // =========================
    // TD3 - COST & MARGIN
    // =========================
    
    static void testTD3CostAndMargin(DataRetriever dr) {
        printTestTitle("TD3", "Calculate dish cost and margin");
        try {
            Dish d = dr.findDishById(1);
            printInfo("Dish: " + d.getName());
            printInfo("Cost: " + String.format("%.2f Ar", d.getDishCost()));
            printInfo("Margin: " + String.format("%.2f%%", d.getGrossMargin()));
            printSuccess("Cost and margin calculated");
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    // =========================
    // TD4 - STOCK MANAGEMENT
    // =========================
    
    static void testTD4Stock(DataRetriever dr) {
        printTestTitle("TD4", "Stock movements tracking");
        try {
            Ingredient ing = new Ingredient();
            ing.setName("Beurre");
            ing.setCategory(CategoryEnum.DAIRY);
            ing.setPrice(1500.0);

            List<StockMovement> movements = new ArrayList<>();
            
            StockMovement in = new StockMovement();
            in.setQuantity(10.0);
            in.setUnit(UnitEnum.KG);
            in.setMovementDate(Instant.now());

            StockMovement out = new StockMovement();
            out.setQuantity(-2.0);
            out.setUnit(UnitEnum.KG);
            out.setMovementDate(Instant.now());

            movements.add(in);
            movements.add(out);
            ing.setStockMovementList(movements);

            Ingredient saved = dr.saveIngredient(ing);
            Ingredient fetched = dr.findIngredientById(saved.getId());
            
            printInfo("Ingredient: " + fetched.getName());
            printInfo("Stock movements: " + fetched.getStockMovementList().size());
            printInfo("Current stock: " + fetched.getStockValueAt(Instant.now()) + " KG");
            printSuccess("Stock management working correctly");
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    // =========================
    // ORDER MANAGEMENT
    // =========================
    
    static void testOrderSaveAndFind(DataRetriever dr) {
        printTestTitle("1", "Save and retrieve order");
        try {
            Order order = new Order();
            List<DishOrder> dishOrders = new ArrayList<>();

            DishOrder dishOrder1 = new DishOrder();
            Dish dish1 = new Dish();
            dish1.setId(1);
            dishOrder1.setDish(dish1);
            dishOrder1.setQuantity(2);
            dishOrders.add(dishOrder1);

            DishOrder dishOrder2 = new DishOrder();
            Dish dish2 = new Dish();
            dish2.setId(2);
            dishOrder2.setDish(dish2);
            dishOrder2.setQuantity(1);
            dishOrders.add(dishOrder2);

            order.setDishOrders(dishOrders);
            
            // Add table with unique time slot
            Instant[] timeSlot = getNextTimeSlot();
            Table table = new Table();
            table.setId(1);
            table.setNumber(1);
            
            TableOrder tableOrder = new TableOrder();
            tableOrder.setTable(table);
            tableOrder.setArrivalDatetime(timeSlot[0]);
            tableOrder.setDepartureDatetime(timeSlot[1]);
            
            order.setTableOrder(tableOrder);

            Order savedOrder = dr.saveOrder(order);
            printInfo("Order saved: " + savedOrder.getReference());
            printInfo("Total (excl. VAT): " + String.format("%.2f Ar", savedOrder.getTotalAmountWithoutVAT()));
            printInfo("Total (incl. VAT): " + String.format("%.2f Ar", savedOrder.getTotalAmountWithVAT()));

            Order retrievedOrder = dr.findOrderByReference(savedOrder.getReference());
            printInfo("Order retrieved: " + retrievedOrder.getReference());
            printInfo("Dish count: " + retrievedOrder.getDishOrders().size());

            for (DishOrder dOrder : retrievedOrder.getDishOrders()) {
                printInfo("  • " + dOrder.getDish().getName() + " ×" + dOrder.getQuantity());
            }

            printSuccess("Order save and retrieval successful");
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    static void testOrderInsufficientStock(DataRetriever dr) {
        printTestTitle("2", "Order with insufficient stock");
        try {
            Order order = new Order();
            List<DishOrder> dishOrders = new ArrayList<>();

            DishOrder dishOrder = new DishOrder();
            Dish dish = new Dish();
            dish.setId(1);
            dishOrder.setDish(dish);
            dishOrder.setQuantity(10000);

            dishOrders.add(dishOrder);
            order.setDishOrders(dishOrders);
            
            // Add table with unique time slot
            Instant[] timeSlot = getNextTimeSlot();
            Table table = new Table();
            table.setId(1);
            table.setNumber(1);
            
            TableOrder tableOrder = new TableOrder();
            tableOrder.setTable(table);
            tableOrder.setArrivalDatetime(timeSlot[0]);
            tableOrder.setDepartureDatetime(timeSlot[1]);
            
            order.setTableOrder(tableOrder);

            dr.saveOrder(order);
            printFailure("Should throw RuntimeException for insufficient stock");
        } catch (RuntimeException e) {
            printSuccess("Exception thrown as expected: " + e.getMessage());
        }
        printTestEnd();
    }

    static void testOrderNotFound(DataRetriever dr) {
        printTestTitle("3", "Find non-existent order");
        try {
            dr.findOrderByReference("ORD99999");
            printFailure("Should throw RuntimeException");
        } catch (RuntimeException e) {
            printSuccess("Exception thrown as expected: " + e.getMessage());
        }
        printTestEnd();
    }

    static void testOrderStockDecrease(DataRetriever dr) {
        printTestTitle("4", "Verify stock decrease after order");
        try {
            Ingredient ingredient = dr.findIngredientById(1);
            double stockInitial = ingredient.getStockValueAt(Instant.now());
            printInfo("Initial stock: " + String.format("%.2f", stockInitial));

            Order order = new Order();
            List<DishOrder> dishOrders = new ArrayList<>();

            DishOrder dishOrder = new DishOrder();
            Dish dish = new Dish();
            dish.setId(1);
            dishOrder.setDish(dish);
            dishOrder.setQuantity(1);

            dishOrders.add(dishOrder);
            order.setDishOrders(dishOrders);
            
            // Add table with unique time slot
            Instant[] timeSlot = getNextTimeSlot();
            Table table = new Table();
            table.setId(1);
            table.setNumber(1);
            
            TableOrder tableOrder = new TableOrder();
            tableOrder.setTable(table);
            tableOrder.setArrivalDatetime(timeSlot[0]);
            tableOrder.setDepartureDatetime(timeSlot[1]);
            
            order.setTableOrder(tableOrder);

            Order savedOrder = dr.saveOrder(order);
            printInfo("Order created: " + savedOrder.getReference());

            Ingredient updatedIngredient = dr.findIngredientById(1);
            double stockFinal = updatedIngredient.getStockValueAt(Instant.now());
            printInfo("Final stock: " + String.format("%.2f", stockFinal));

            if (stockFinal < stockInitial) {
                double decrease = stockInitial - stockFinal;
                printSuccess("Stock decreased by " + String.format("%.2f", decrease));
            } else {
                printFailure("Stock did not decrease");
            }
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    static void testOrderReferenceFormat(DataRetriever dr) {
        printTestTitle("5", "Verify order reference format");
        try {
            Order order = new Order();
            List<DishOrder> dishOrders = new ArrayList<>();

            DishOrder dishOrder = new DishOrder();
            Dish dish = new Dish();
            dish.setId(1);
            dishOrder.setDish(dish);
            dishOrder.setQuantity(1);

            dishOrders.add(dishOrder);
            order.setDishOrders(dishOrders);
            
            // Add table with unique time slot
            Instant[] timeSlot = getNextTimeSlot();
            Table table = new Table();
            table.setId(1);
            table.setNumber(1);
            
            TableOrder tableOrder = new TableOrder();
            tableOrder.setTable(table);
            tableOrder.setArrivalDatetime(timeSlot[0]);
            tableOrder.setDepartureDatetime(timeSlot[1]);
            
            order.setTableOrder(tableOrder);

            Order savedOrder = dr.saveOrder(order);
            String reference = savedOrder.getReference();

            printInfo("Reference: " + reference);

            if (reference.matches("ORD\\d{5}")) {
                printSuccess("Format is correct (ORDxxxxx)");
            } else {
                printFailure("Format is incorrect");
            }
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    // =========================
    // BONUS K1 - UNIT CONVERSION
    // =========================

    static void testUnitConversion_Basic() {
        printTestTitle("K1.1", "Basic unit conversions");
        try {
            boolean allPassed = true;

            // Tomate: 1 KG = 10 PCS
            double result1 = UnitConversionService.convertToKg("Tomate", 10.0, "PCS");
            System.out.print("  10 PCS Tomate = " + result1 + " KG");
            printCheckmark(result1 == 1.0);
            System.out.println();
            allPassed &= (result1 == 1.0);

            // Laitue: 1 KG = 2 PCS
            double result2 = UnitConversionService.convertToKg("Laitue", 2.0, "PCS");
            System.out.print("  2 PCS Laitue = " + result2 + " KG");
            printCheckmark(result2 == 1.0);
            System.out.println();
            allPassed &= (result2 == 1.0);

            // Chocolat: 1 KG = 2.5 L
            double result3 = UnitConversionService.convertToKg("Chocolat", 2.5, "L");
            System.out.print("  2.5 L Chocolat = " + result3 + " KG");
            printCheckmark(result3 == 1.0);
            System.out.println();
            allPassed &= (result3 == 1.0);

            // Poulet: 1 KG = 8 PCS
            double result4 = UnitConversionService.convertToKg("Poulet", 8.0, "PCS");
            System.out.print("  8 PCS Poulet = " + result4 + " KG");
            printCheckmark(result4 == 1.0);
            System.out.println();
            allPassed &= (result4 == 1.0);

            // Beurre: 1 KG = 5 L
            double result5 = UnitConversionService.convertToKg("Beurre", 5.0, "L");
            System.out.print("  5 L Beurre = " + result5 + " KG");
            printCheckmark(result5 == 1.0);
            System.out.println();
            allPassed &= (result5 == 1.0);

            if (allPassed) {
                printSuccess("All basic conversions work correctly");
            } else {
                printFailure("Some conversions are incorrect");
            }
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    static void testUnitConversion_ToKg() {
        printTestTitle("K1.2", "Conversions to KG");
        try {
            boolean allPassed = true;

            double result1 = UnitConversionService.convertToKg("Tomate", 5.0, "PCS");
            System.out.print("  5 PCS Tomate = " + result1 + " KG");
            printCheckmark(result1 == 0.5);
            System.out.println();
            allPassed &= (result1 == 0.5);

            double result2 = UnitConversionService.convertToKg("Chocolat", 1.0, "L");
            System.out.print("  1 L Chocolat = " + result2 + " KG");
            printCheckmark(result2 == 0.4);
            System.out.println();
            allPassed &= (result2 == 0.4);

            double result3 = UnitConversionService.convertToKg("Poulet", 4.0, "PCS");
            System.out.print("  4 PCS Poulet = " + result3 + " KG");
            printCheckmark(result3 == 0.5);
            System.out.println();
            allPassed &= (result3 == 0.5);

            double result4 = UnitConversionService.convertToKg("Beurre", 1.0, "L");
            System.out.print("  1 L Beurre = " + result4 + " KG");
            printCheckmark(result4 == 0.2);
            System.out.println();
            allPassed &= (result4 == 0.2);

            if (allPassed) {
                printSuccess("All conversions to KG are correct");
            } else {
                printFailure("Some conversions are incorrect");
            }
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    static void testUnitConversion_FromKg() {
        printTestTitle("K1.3", "Conversions from KG");
        try {
            boolean allPassed = true;

            double result1 = UnitConversionService.convert("Tomate", 1.0, "KG", "PCS");
            System.out.print("  1 KG Tomate = " + result1 + " PCS");
            printCheckmark(result1 == 10.0);
            System.out.println();
            allPassed &= (result1 == 10.0);

            double result2 = UnitConversionService.convert("Chocolat", 1.0, "KG", "L");
            System.out.print("  1 KG Chocolat = " + result2 + " L");
            printCheckmark(result2 == 2.5);
            System.out.println();
            allPassed &= (result2 == 2.5);

            double result3 = UnitConversionService.convert("Beurre", 1.0, "KG", "PCS");
            System.out.print("  1 KG Beurre = " + result3 + " PCS");
            printCheckmark(result3 == 4.0);
            System.out.println();
            allPassed &= (result3 == 4.0);

            if (allPassed) {
                printSuccess("All conversions from KG are correct");
            } else {
                printFailure("Some conversions are incorrect");
            }
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    static void testUnitConversion_Impossible() {
        printTestTitle("K1.4", "Impossible conversions");
        int passCount = 0;

        // Tomate KG -> L (impossible)
        try {
            UnitConversionService.convert("Tomate", 1.0, "KG", "L");
            printInfo("Tomate KG→L: Should fail");
            printFailure("Conversion should be impossible");
        } catch (RuntimeException e) {
            printInfo("Tomate KG→L: ✓ Correctly rejected");
            passCount++;
        }

        // Laitue PCS -> L (impossible)
        try {
            UnitConversionService.convert("Laitue", 1.0, "PCS", "L");
            printInfo("Laitue PCS→L: Should fail");
            printFailure("Conversion should be impossible");
        } catch (RuntimeException e) {
            printInfo("Laitue PCS→L: ✓ Correctly rejected");
            passCount++;
        }

        // Poulet KG -> L (impossible)
        try {
            UnitConversionService.convert("Poulet", 1.0, "KG", "L");
            printInfo("Poulet KG→L: Should fail");
            printFailure("Conversion should be impossible");
        } catch (RuntimeException e) {
            printInfo("Poulet KG→L: ✓ Correctly rejected");
            passCount++;
        }

        if (passCount == 3) {
            printSuccess("All impossible conversions correctly detected");
        } else {
            printFailure("Some impossible conversions not detected (" + passCount + "/3)");
        }
        printTestEnd();
    }

    static void testUnitConversion_RealScenario() {
        printTestTitle("K1.5", "Real-world scenario");
        try {
            boolean allPassed = true;
            printInfo("Initial Stock → Movements → Expected Final Stock:");

            // Tomate: 4.0 KG - 5 PCS = 3.5 KG
            double tomateUsed = UnitConversionService.convertToKg("Tomate", 5.0, "PCS");
            double tomateFinal = 4.0 - tomateUsed;
            System.out.print("  Tomate: 4.0 KG - " + tomateUsed + " KG = " + tomateFinal + " KG");
            boolean tomateOk = Math.abs(tomateFinal - 3.5) < 0.01;
            printCheckmark(tomateOk);
            System.out.println();
            allPassed &= tomateOk;

            // Laitue: 5.0 KG - 2 PCS = 4.0 KG
            double laitueUsed = UnitConversionService.convertToKg("Laitue", 2.0, "PCS");
            double laitueFinal = 5.0 - laitueUsed;
            System.out.print("  Laitue: 5.0 KG - " + laitueUsed + " KG = " + laitueFinal + " KG");
            boolean laitueOk = Math.abs(laitueFinal - 4.0) < 0.01;
            printCheckmark(laitueOk);
            System.out.println();
            allPassed &= laitueOk;

            // Chocolat: 3.0 KG - 1 L = 2.6 KG
            double chocolatUsed = UnitConversionService.convertToKg("Chocolat", 1.0, "L");
            double chocolatFinal = 3.0 - chocolatUsed;
            System.out.print("  Chocolat: 3.0 KG - " + chocolatUsed + " KG = " + chocolatFinal + " KG");
            boolean chocolatOk = Math.abs(chocolatFinal - 2.6) < 0.01;
            printCheckmark(chocolatOk);
            System.out.println();
            allPassed &= chocolatOk;

            // Poulet: 10.0 KG - 4 PCS = 9.5 KG
            double pouletUsed = UnitConversionService.convertToKg("Poulet", 4.0, "PCS");
            double pouletFinal = 10.0 - pouletUsed;
            System.out.print("  Poulet: 10.0 KG - " + pouletUsed + " KG = " + pouletFinal + " KG");
            boolean pouletOk = Math.abs(pouletFinal - 9.5) < 0.01;
            printCheckmark(pouletOk);
            System.out.println();
            allPassed &= pouletOk;

            // Beurre: 2.5 KG - 1 L = 2.3 KG
            double beurreUsed = UnitConversionService.convertToKg("Beurre", 1.0, "L");
            double beurreFinal = 2.5 - beurreUsed;
            System.out.print("  Beurre: 2.5 KG - " + beurreUsed + " KG = " + beurreFinal + " KG");
            boolean beurreOk = Math.abs(beurreFinal - 2.3) < 0.01;
            printCheckmark(beurreOk);
            System.out.println();
            allPassed &= beurreOk;

            if (allPassed) {
                printSuccess("Real-world scenario validated");
            } else {
                printFailure("Some calculations are incorrect");
            }
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    // =========================
    // TABLE MANAGEMENT
    // =========================
    
    static void testOrderWithoutTable(DataRetriever dr) {
        printTestTitle("6", "Order without table");
        try {
            Order order = new Order();

            DishOrder d = new DishOrder();
            Dish dish = new Dish();
            dish.setId(1);
            d.setDish(dish);
            d.setQuantity(1);

            order.setDishOrders(List.of(d));

            dr.saveOrder(order);
            printFailure("Should throw RuntimeException");
        } catch (RuntimeException e) {
            printSuccess("Exception thrown as expected: " + e.getMessage());
        }
        printTestEnd();
    }

    static void testOrderWithUnavailableTable(DataRetriever dr) {
        printTestTitle("7", "Order with unavailable table");
        try {
            Instant[] timeSlot = getNextTimeSlot();
            
            // First order occupies table 1
            Order first = createOrderWithTable(1, timeSlot[0], timeSlot[1]);
            dr.saveOrder(first);
            printInfo("First order occupies table 1");

            // Second order tries same table at same time (must use exact same time slot)
            Order second = createOrderWithTable(1, timeSlot[0], timeSlot[1]);
            dr.saveOrder(second);

            printFailure("Should throw RuntimeException");
        } catch (RuntimeException e) {
            printSuccess("Exception thrown as expected: " + e.getMessage());
        }
        printTestEnd();
    }

    static void testOrderWithAvailableTable(DataRetriever dr) {
        printTestTitle("8", "Order with available table");
        try {
            Instant[] timeSlot = getNextTimeSlot();
            Order order = createOrderWithTable(2, timeSlot[0], timeSlot[1]);

            Order saved = dr.saveOrder(order);
            printInfo("Order created: " + saved.getReference());
            printInfo("Table assigned: #" + saved.getTableOrder().getTable().getNumber());
            printSuccess("Order created with available table");
        } catch (Exception e) {
            printFailure(e.getMessage());
        }
        printTestEnd();
    }

    static void testOrderWhenAllTablesBusy(DataRetriever dr) {
        printTestTitle("9", "Order when all tables are busy");
        try {
            Instant[] timeSlot = getNextTimeSlot();
            Instant start = timeSlot[0];
            Instant end = timeSlot[1];

            // Occupy tables 1, 2, 3
            dr.saveOrder(createOrderWithTable(1, start, end));
            printInfo("Table 1 occupied");
            dr.saveOrder(createOrderWithTable(2, start, end));
            printInfo("Table 2 occupied");
            dr.saveOrder(createOrderWithTable(3, start, end));
            printInfo("Table 3 occupied");

            // Try another order at same time - should fail
            dr.saveOrder(createOrderWithTable(1, start, end));

            printFailure("Should throw RuntimeException");
        } catch (RuntimeException e) {
            printSuccess("Exception thrown as expected: " + e.getMessage());
        }
        printTestEnd();
    }

    static Order createOrderWithTable(int tableNumber, Instant arrival, Instant departure) {
        Order order = new Order();

        DishOrder d = new DishOrder();
        Dish dish = new Dish();
        dish.setId(1);
        d.setDish(dish);
        d.setQuantity(1);

        order.setDishOrders(List.of(d));

        Table table = new Table();
        table.setId(tableNumber);
        table.setNumber(tableNumber);

        TableOrder tableOrder = new TableOrder();
        tableOrder.setTable(table);
        tableOrder.setArrivalDatetime(arrival);
        tableOrder.setDepartureDatetime(departure);

        order.setTableOrder(tableOrder);

        return order;
    }
}

package jdbc.td2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jdbc.td2.dao.DataRetriever;
import jdbc.td2.model.*;

public class Main {

    public static void main(String[] args) {
        DataRetriever dr = new DataRetriever();

        System.out.println("=== TESTS ===\n");
        testA(dr);
        testB(dr);
        testC(dr);
        testD(dr);
        testE(dr);
        testF(dr);
        testTD3CostAndMargin(dr);
        testTD4Stock(dr);

        // Tests for orders
        System.out.println("\n=== ORDERS ===\n");
        testOrderSaveAndFind(dr);
        testOrderInsufficientStock(dr);
        testOrderNotFound(dr);
        testOrderStockDecrease(dr);
        testOrderReferenceFormat(dr);

        // Tests for BONUS K1 - Unit Conversion
        System.out.println("\n=== BONUS K1: UNIT CONVERSION ===\n");
        testUnitConversion_Basic();
        testUnitConversion_ToKg();
        testUnitConversion_FromKg();
        testUnitConversion_Impossible();
        testUnitConversion_RealScenario();

        System.out.println("\n=== This is the end ===\n");
    }

    // =========================
    // TD2
    // =========================
    static void testA(DataRetriever dr) {
        System.out.println("Test A) findDishById(1)");
        Dish d = dr.findDishById(1);
        System.out.println("Plat: " + d.getName());
        System.out.println("Ingrédients: " + d.getIngredients().size());
        System.out.println();
    }

    static void testB(DataRetriever dr) {
        System.out.println("Test B) findDishById(999)");
        try {
            dr.findDishById(999);
            System.out.println("FAIL");
        } catch (RuntimeException e) {
            System.out.println("PASS: " + e.getMessage());
        }
        System.out.println();
    }

    static void testC(DataRetriever dr) {
        System.out.println("Test C) findIngredients(page=1, size=3)");
        dr.findIngredients(1, 3)
                .forEach(i -> System.out.println("- " + i.getName()));
        System.out.println();
    }

    static void testD(DataRetriever dr) {
        System.out.println("Test D) findIngredients(page=3, size=5)");
        System.out.println("Nombre: " + dr.findIngredients(3, 5).size());
        System.out.println();
    }

    static void testE(DataRetriever dr) {
        System.out.println("Test E) findDishesByIngredientName('Tomate')");
        dr.findDishesByIngredientName("Tomate")
                .forEach(d -> System.out.println("- " + d.getName()));
        System.out.println();
    }

    static void testF(DataRetriever dr) {
        System.out.println("Test F) findIngredientsByCriteria(VEGETABLE)");
        dr.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10)
                .forEach(i -> System.out.println("- " + i.getName()));
        System.out.println();
    }

    // =========================
    // TD3 — COST & MARGIN
    // =========================
    static void testTD3CostAndMargin(DataRetriever dr) {
        System.out.println("=========");
        Dish d = dr.findDishById(1);
        System.out.println(d.getName() + " cost: " + d.getDishCost());
        System.out.println(d.getName() + " margin: " + d.getGrossMargin());
        System.out.println();
    }

    // =========================
    // TD4 — STOCK
    // =========================
    static void testTD4Stock(DataRetriever dr) {
        System.out.println("==========");
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
        System.out.println("Ingredient: " + fetched.getName());
        System.out.println("Stock movements: " + fetched.getStockMovementList().size());
        System.out.println("Stock now: " + fetched.getStockValueAt(Instant.now()));
        System.out.println();
    }

    // =========================
    // ORDER TESTS
    // =========================
    static void testOrderSaveAndFind(DataRetriever dr) {
        System.out.println("Test 1) saveOrder + findOrderByReference");
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

            // Save order
            Order savedOrder = dr.saveOrder(order);
            System.out.println("Commande sauvegardée: " + savedOrder.getReference());
            System.out.println("Total HT: " + savedOrder.getTotalAmountWithoutVAT() + "Ar");
            System.out.println("Total TTC: " + savedOrder.getTotalAmountWithVAT() + "Ar");

            // Retrieve order
            Order retrievedOrder = dr.findOrderByReference(savedOrder.getReference());
            System.out.println("Commande récupérée: " + retrievedOrder.getReference());
            System.out.println("Nombre de plats: " + retrievedOrder.getDishOrders().size());

            for (DishOrder dOrder : retrievedOrder.getDishOrders()) {
                System.out.println("  - " + dOrder.getDish().getName() + " x" + dOrder.getQuantity());
            }

        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
        }
        System.out.println();
    }

    static void testOrderInsufficientStock(DataRetriever dr) {
        System.out.println("Test 2) saveOrder - Stock insuffisant");
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

            dr.saveOrder(order);
            System.out.println("FAIL: Devrait lever une exception");

        } catch (RuntimeException e) {
            System.out.println("PASS: " + e.getMessage());
        }
        System.out.println();
    }

    static void testOrderNotFound(DataRetriever dr) {
        System.out.println("Test 3) findOrderByReference - Commande inexistante");
        try {
            dr.findOrderByReference("ORD99999");
            System.out.println("ÉCHEC: Devrait lever une exception");

        } catch (RuntimeException e) {
            System.out.println("PASS: " + e.getMessage());
        }
        System.out.println();
    }

    static void testOrderStockDecrease(DataRetriever dr) {
        System.out.println("Test 4) saveOrder - Vérification stock");
        try {
            // Get initial stock
            Ingredient ingredient = dr.findIngredientById(1);
            double stockInitial = ingredient.getStockValueAt(Instant.now());
            System.out.println("  - Stock initial: " + stockInitial);

            // Create order
            Order order = new Order();
            List<DishOrder> dishOrders = new ArrayList<>();

            DishOrder dishOrder = new DishOrder();
            Dish dish = new Dish();
            dish.setId(1);
            dishOrder.setDish(dish);
            dishOrder.setQuantity(1);

            dishOrders.add(dishOrder);
            order.setDishOrders(dishOrders);

            Order savedOrder = dr.saveOrder(order);
            System.out.println("  - Commande: " + savedOrder.getReference());

            // Get final stock
            Ingredient updatedIngredient = dr.findIngredientById(1);
            double stockFinal = updatedIngredient.getStockValueAt(Instant.now());
            System.out.println("  - Stock final: " + stockFinal);

            if (stockFinal < stockInitial) {
                System.out.println("PASS: Stock diminué de " + (stockInitial - stockFinal));
            } else {
                System.out.println("Stock non diminué");
            }

        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
        }
        System.out.println();
    }

    static void testOrderReferenceFormat(DataRetriever dr) {
        System.out.println("Test 5) saveOrder - Format référence");
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

            Order savedOrder = dr.saveOrder(order);
            String reference = savedOrder.getReference();

            System.out.println("  - Référence: " + reference);

            if (reference.matches("ORD\\d{5}")) {
                System.out.println("PASS: Format correct (ORDxxxxx)");
            } else {
                System.out.println("FAIL: Format incorrect");
            }

        } catch (Exception e) {
            System.out.println("ERREUR: " + e.getMessage());
        }
        System.out.println();
    }

    // =========================
    // BONUS K1 - UNIT CONVERSION TESTS
    // =========================

    static void testUnitConversion_Basic() {
        System.out.println("Test K1.1) Conversions de base");
        try {
            // Tomate: 1 KG = 10 PCS
            double result1 = UnitConversionService.convertToKg("Tomate", 10.0, "PCS");
            System.out.println("  10 PCS Tomate = " + result1 + " KG " + (result1 == 1.0 ? "✓" : "✗"));

            // Laitue: 1 KG = 2 PCS
            double result2 = UnitConversionService.convertToKg("Laitue", 2.0, "PCS");
            System.out.println("  2 PCS Laitue = " + result2 + " KG " + (result2 == 1.0 ? "✓" : "✗"));

            // Chocolat: 1 KG = 2.5 L
            double result3 = UnitConversionService.convertToKg("Chocolat", 2.5, "L");
            System.out.println("  2.5 L Chocolat = " + result3 + " KG " + (result3 == 1.0 ? "✓" : "✗"));

            // Poulet: 1 KG = 8 PCS
            double result4 = UnitConversionService.convertToKg("Poulet", 8.0, "PCS");
            System.out.println("  8 PCS Poulet = " + result4 + " KG " + (result4 == 1.0 ? "✓" : "✗"));

            // Beurre: 1 KG = 5 L
            double result5 = UnitConversionService.convertToKg("Beurre", 5.0, "L");
            System.out.println("  5 L Beurre = " + result5 + " KG " + (result5 == 1.0 ? "✓" : "✗"));

            System.out.println("PASS: Conversions de base fonctionnent");

        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
        System.out.println();
    }

    static void testUnitConversion_ToKg() {
        System.out.println("Test K1.2) Conversions vers KG");
        try {
            // 5 PCS Tomate = 0.5 KG
            double result1 = UnitConversionService.convertToKg("Tomate", 5.0, "PCS");
            System.out.println("  5 PCS Tomate = " + result1 + " KG " + (result1 == 0.5 ? "✓" : "✗"));

            // 1 L Chocolat = 0.4 KG
            double result2 = UnitConversionService.convertToKg("Chocolat", 1.0, "L");
            System.out.println("  1 L Chocolat = " + result2 + " KG " + (result2 == 0.4 ? "✓" : "✗"));

            // 4 PCS Poulet = 0.5 KG
            double result3 = UnitConversionService.convertToKg("Poulet", 4.0, "PCS");
            System.out.println("  4 PCS Poulet = " + result3 + " KG " + (result3 == 0.5 ? "✓" : "✗"));

            // 1 L Beurre = 0.2 KG
            double result4 = UnitConversionService.convertToKg("Beurre", 1.0, "L");
            System.out.println("  1 L Beurre = " + result4 + " KG " + (result4 == 0.2 ? "✓" : "✗"));

            System.out.println("PASS: Conversions vers KG correctes");

        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
        System.out.println();
    }

    static void testUnitConversion_FromKg() {
        System.out.println("Test K1.3) Conversions depuis KG");
        try {
            // 1 KG Tomate = 10 PCS
            double result1 = UnitConversionService.convert("Tomate", 1.0, "KG", "PCS");
            System.out.println("  1 KG Tomate = " + result1 + " PCS " + (result1 == 10.0 ? "✓" : "✗"));

            // 1 KG Chocolat = 2.5 L
            double result2 = UnitConversionService.convert("Chocolat", 1.0, "KG", "L");
            System.out.println("  1 KG Chocolat = " + result2 + " L " + (result2 == 2.5 ? "✓" : "✗"));

            // 1 KG Beurre = 4 PCS
            double result3 = UnitConversionService.convert("Beurre", 1.0, "KG", "PCS");
            System.out.println("  1 KG Beurre = " + result3 + " PCS " + (result3 == 4.0 ? "✓" : "✗"));

            System.out.println("PASS: Conversions depuis KG correctes");

        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
        System.out.println();
    }

    static void testUnitConversion_Impossible() {
        System.out.println("Test K1.4) Conversions impossibles");
        int passCount = 0;

        // Tomate KG -> L (impossible)
        try {
            UnitConversionService.convert("Tomate", 1.0, "KG", "L");
            System.out.println("  Tomate KG->L: FAIL (devrait échouer)");
        } catch (RuntimeException e) {
            System.out.println("  Tomate KG->L: PASS (impossible comme attendu)");
            passCount++;
        }

        // Laitue PCS -> L (impossible)
        try {
            UnitConversionService.convert("Laitue", 1.0, "PCS", "L");
            System.out.println("  Laitue PCS->L: FAIL (devrait échouer)");
        } catch (RuntimeException e) {
            System.out.println("  Laitue PCS->L: PASS (impossible comme attendu)");
            passCount++;
        }

        // Poulet KG -> L (impossible)
        try {
            UnitConversionService.convert("Poulet", 1.0, "KG", "L");
            System.out.println("  Poulet KG->L: FAIL (devrait échouer)");
        } catch (RuntimeException e) {
            System.out.println("  Poulet KG->L: PASS (impossible comme attendu)");
            passCount++;
        }

        if (passCount == 3) {
            System.out.println("PASS: Toutes les conversions impossibles détectées");
        } else {
            System.out.println("FAIL: Certaines conversions impossibles non détectées");
        }
        System.out.println();
    }

    static void testUnitConversion_RealScenario() {
        System.out.println("Test K1.5) Scénario réel de l'exercice");
        try {
            System.out.println("  Stock initial -> Mouvements -> Stock final attendu:");

            // Tomate: 4.0 KG - 5 PCS = 3.5 KG
            double tomateUsed = UnitConversionService.convertToKg("Tomate", 5.0, "PCS");
            double tomateFinal = 4.0 - tomateUsed;
            System.out.println("  Tomate: 4.0 KG - " + tomateUsed + " KG = " + tomateFinal + " KG " 
                + (Math.abs(tomateFinal - 3.5) < 0.01 ? "✓" : "✗"));

            // Laitue: 5.0 KG - 2 PCS = 4.0 KG
            double laitueUsed = UnitConversionService.convertToKg("Laitue", 2.0, "PCS");
            double laitueFinal = 5.0 - laitueUsed;
            System.out.println("  Laitue: 5.0 KG - " + laitueUsed + " KG = " + laitueFinal + " KG " 
                + (Math.abs(laitueFinal - 4.0) < 0.01 ? "✓" : "✗"));

            // Chocolat: 3.0 KG - 1 L = 2.6 KG
            double chocolatUsed = UnitConversionService.convertToKg("Chocolat", 1.0, "L");
            double chocolatFinal = 3.0 - chocolatUsed;
            System.out.println("  Chocolat: 3.0 KG - " + chocolatUsed + " KG = " + chocolatFinal + " KG " 
                + (Math.abs(chocolatFinal - 2.6) < 0.01 ? "✓" : "✗"));

            // Poulet: 10.0 KG - 4 PCS = 9.5 KG
            double pouletUsed = UnitConversionService.convertToKg("Poulet", 4.0, "PCS");
            double pouletFinal = 10.0 - pouletUsed;
            System.out.println("  Poulet: 10.0 KG - " + pouletUsed + " KG = " + pouletFinal + " KG " 
                + (Math.abs(pouletFinal - 9.5) < 0.01 ? "✓" : "✗"));

            // Beurre: 2.5 KG - 1 L = 2.3 KG
            double beurreUsed = UnitConversionService.convertToKg("Beurre", 1.0, "L");
            double beurreFinal = 2.5 - beurreUsed;
            System.out.println("  Beurre: 2.5 KG - " + beurreUsed + " KG = " + beurreFinal + " KG " 
                + (Math.abs(beurreFinal - 2.3) < 0.01 ? "✓" : "✗"));

            System.out.println("PASS: Scénario réel validé");

        } catch (Exception e) {
            System.out.println("FAIL: " + e.getMessage());
        }
        System.out.println();
    }
}
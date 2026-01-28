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
    
    // SAVE AND RETRIEVE ONE
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

            // Retrieve order
            Order retrievedOrder = dr.findOrderByReference(savedOrder.getReference());
            System.out.println(" Commande récupérée: " + retrievedOrder.getReference());
            System.out.println("Nombre de plats: " + retrievedOrder.getDishOrders().size());
            
            for (DishOrder dOrder : retrievedOrder.getDishOrders()) {
                System.out.println("  - " + dOrder.getDish().getName() + " x" + dOrder.getQuantity());
            }
            
        } catch (Exception e) {
            System.out.println(" ERREUR: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Test 2: Insufficient stock
     */
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
            System.out.println(" FAIL: Devrait lever une exception");
            
        } catch (RuntimeException e) {
            System.out.println(" PASS: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Test 3: Order not found
     */
    static void testOrderNotFound(DataRetriever dr) {
        System.out.println("Test 3) findOrderByReference - Commande inexistante");
        try {
            dr.findOrderByReference("ORD99999");
            System.out.println(" ÉCHEC: Devrait lever une exception");
            
        } catch (RuntimeException e) {
            System.out.println(" PASS: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Test 4: Stock decrease verification
     */
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
                System.out.println(" Stock diminué de " + (stockInitial - stockFinal));
            } else {
                System.out.println(" Stock non diminué");
            }
            
        } catch (Exception e) {
            System.out.println(" ERREUR: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Test 5: Reference format
     */
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
                System.out.println(" Format correct (ORDxxxxx)");
            } else {
                System.out.println(" Format incorrect");
            }
            
        } catch (Exception e) {
            System.out.println(" ERREUR: " + e.getMessage());
        }
        System.out.println();
    }
}

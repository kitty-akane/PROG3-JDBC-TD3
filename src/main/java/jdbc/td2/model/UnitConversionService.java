package jdbc.td2.model;

import java.util.HashMap;
import java.util.Map;

public class UnitConversionService {

    private String ingredientName;
    private Double kgToPcs;
    private Double kgToL;

    private static final Map<String, UnitConversionService> CONVERSIONS = new HashMap<>();

    static {
        CONVERSIONS.put("Tomate", new UnitConversionService("Tomate", 10.0, null));
        CONVERSIONS.put("Laitue", new UnitConversionService("Laitue", 2.0, null));
        CONVERSIONS.put("Chocolat", new UnitConversionService("Chocolat", 10.0, 2.5));
        CONVERSIONS.put("Poulet", new UnitConversionService("Poulet", 8.0, null));
        CONVERSIONS.put("Beurre", new UnitConversionService("Beurre", 4.0, 5.0));
    }

    private UnitConversionService(String ingredientName, Double kgToPcs, Double kgToL) {
        this.ingredientName = ingredientName;
        this.kgToPcs = kgToPcs;
        this.kgToL = kgToL;
    }

    public Double getKgToPcs() {
        return kgToPcs;
    }

    public Double getKgToL() {
        return kgToL;
    }

    public static UnitConversionService getConversion(String ingredientName) {
        return CONVERSIONS.get(ingredientName);
    }

    public static double convert(
            String ingredientName,
            double quantity,
            String unitSource,
            String unitTarget
    ) {
        if (unitSource.equalsIgnoreCase(unitTarget)) {
            return quantity;
        }

        UnitConversionService conversion = CONVERSIONS.get(ingredientName);
        if (conversion == null) {
            throw new RuntimeException("Pas de conversion pour: " + ingredientName);
        }

        double quantityInKg;

        switch (unitSource.toUpperCase()) {
            case "KG" -> quantityInKg = quantity;
            case "PCS" -> {
                if (conversion.getKgToPcs() == null) {
                    throw new RuntimeException("Conversion PCS impossible pour: " + ingredientName);
                }
                quantityInKg = quantity / conversion.getKgToPcs();
            }
            case "L" -> {
                if (conversion.getKgToL() == null) {
                    throw new RuntimeException("Conversion L impossible pour: " + ingredientName);
                }
                quantityInKg = quantity / conversion.getKgToL();
            }
            default -> throw new RuntimeException("Unité inconnue: " + unitSource);
        }

        switch (unitTarget.toUpperCase()) {
            case "KG" -> {
                return quantityInKg;
            }
            case "PCS" -> {
                if (conversion.getKgToPcs() == null) {
                    throw new RuntimeException("Conversion PCS impossible pour: " + ingredientName);
                }
                return quantityInKg * conversion.getKgToPcs();
            }
            case "L" -> {
                if (conversion.getKgToL() == null) {
                    throw new RuntimeException("Conversion L impossible pour: " + ingredientName);
                }
                return quantityInKg * conversion.getKgToL();
            }
            default -> throw new RuntimeException("Unité inconnue: " + unitTarget);
        }
    }

    public static double convertToKg(
            String ingredientName,
            double quantity,
            String unit
    ) {
        return convert(ingredientName, quantity, unit, "KG");
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public void setKgToPcs(Double kgToPcs) {
        this.kgToPcs = kgToPcs;
    }

    public void setKgToL(Double kgToL) {
        this.kgToL = kgToL;
    }

    public String getIngredientName() {
        return ingredientName;
    }
}


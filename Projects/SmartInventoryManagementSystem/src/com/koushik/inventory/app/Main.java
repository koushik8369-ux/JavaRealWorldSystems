
package com.koushik.inventory.app;

import com.koushik.inventory.model.Product;
import com.koushik.inventory.service.InventoryService;

public class Main {

    public static void main(String[] args) {

        InventoryService inventory = new InventoryService();

        inventory.addProduct(new Product(
                "P1001", "Keyboard", "Electronics", 800, 10, 5));

        inventory.addProduct(new Product(
                "P1002", "Mouse", "Electronics", 400, 3, 5));

        System.out.println("=== SMART INVENTORY SYSTEM ===");

        for (Product product : inventory.getAllProducts()) {
            System.out.println(product);
            System.out.println("-------------------------");
        }

        System.out.println("Low-stock products: "
                + inventory.getLowStockProducts().size());

        System.out.println("Total inventory value: Rs. "
                + inventory.getTotalInventoryValue());
    }
}


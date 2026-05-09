package com.minimall.controller;

import com.minimall.model.Product;
import com.minimall.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String products() {
        return "admin/products";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("isEdit", false);
        model.addAttribute("productId", "");
        return "admin/product-form";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable String id, Model model) {
        model.addAttribute("productId", id);
        return "admin/product-detail";
    }

    @GetMapping("/{id}/edit")
    public String editProduct(@PathVariable String id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("isEdit", true);
        model.addAttribute("productId", id);
        return "admin/product-form";
    }
}
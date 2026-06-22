package com.tiendasara.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.tiendasara.models.Category;
import com.tiendasara.models.Mark;
import com.tiendasara.models.Product;
import com.tiendasara.models.ProductDto;
import com.tiendasara.services.CategoryRepository;
import com.tiendasara.services.MarkRepository;
import com.tiendasara.services.ProductRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;
    private final CategoryRepository repo2;
    private final MarkRepository repo3;

    // Inyección limpia por constructor libre de advertencias
    public ProductController(ProductRepository repo, CategoryRepository repo2, MarkRepository repo3) {
        this.repo = repo;
        this.repo2 = repo2;
        this.repo3 = repo3;
    }

    public List<Category> getListCategories(){
        return repo2.findAll(Sort.by(Sort.Direction.DESC, "id"));    
    }

    public List<Mark> getListMarks(){
        return repo3.findAll(Sort.by(Sort.Direction.DESC, "id"));    
    }

    // 1. LISTAR PRODUCTOS
    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        // Quitamos el ordenamiento por "id" para solucionar el error de columna inválida
        List<Product> products = repo.findAll();
        model.addAttribute("products", products);
        return "products/index"; 
    }
    
    // 2. FORMULARIO DE CREACIÓN
    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        model.addAttribute("categories", getListCategories());
        model.addAttribute("marks", getListMarks());
        return "products/CreateProduct";
    }
    
    // 3. GUARDAR PRODUCTO NUEVO
    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result,
            Model model
            ) {
        
        if (productDto.getImageFile() == null || productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
        }
        
        if (result.hasErrors()) {
            model.addAttribute("categories", getListCategories());
            model.addAttribute("marks", getListMarks());
            return "products/CreateProduct";
        }
        
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
        
        try {
            String uploadDir = "src/main/resources/static/images/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            System.out.println("Exception saving image: " + ex.getMessage());
        }
        
        Product product = new Product();
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setAmount(productDto.getAmount());
        product.setCategory(productDto.getCategory());
        product.setMark(productDto.getMark());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);
        
        repo.save(product);
        return "redirect:/products";
    }
    
    // 4. FORMULARIO DE EDICIÓN
    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Product product = repo.findById(id).orElseThrow();
            model.addAttribute("product", product);
            model.addAttribute("categories", getListCategories());
            model.addAttribute("marks", getListMarks());
            
            ProductDto productDto = new ProductDto();
            productDto.setDescription(product.getDescription());
            productDto.setPrice(product.getPrice());
            productDto.setAmount(product.getAmount());
            productDto.setCategory(product.getCategory());
            productDto.setMark(product.getMark());
            
            model.addAttribute("productDto", productDto);
        } catch(Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }
    
    // 5. ACTUALIZAR PRODUCTO EXISITENTE
    @PostMapping("/edit")
    public String updateProduct(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute ProductDto productDto,
            BindingResult result
            ) {
        
        try {
            Product product = repo.findById(id).orElseThrow();
            model.addAttribute("product", product);
            
            if (result.hasErrors()) {
                model.addAttribute("categories", getListCategories());
                model.addAttribute("marks", getListMarks());
                return "products/EditProduct";
            }
            
            if (productDto.getImageFile() != null && !productDto.getImageFile().isEmpty()) {
                String uploadDir = "src/main/resources/static/images/";
                Path oldImagePath = Paths.get(uploadDir).resolve(product.getImageFileName());
                try {
                    Files.deleteIfExists(oldImagePath);
                } catch(Exception ex) {
                    System.out.println("Exception deleting old image: " + ex.getMessage());
                }
                
                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
                                    
                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir).resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }
            
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());
            product.setAmount(productDto.getAmount());
            product.setCategory(productDto.getCategory());
            product.setMark(productDto.getMark());
            
            repo.save(product);
        } catch(Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return "redirect:/products";
    }
    
    // 6. ELIMINAR UN PRODUCTO
    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Product product = repo.findById(id).orElseThrow();
            Path imagePath = Paths.get("src/main/resources/static/images/").resolve(product.getImageFileName());
            try {
                Files.deleteIfExists(imagePath);
            } catch(Exception ex) {
                System.out.println("Exception deleting image file: " + ex.getMessage());
            }
            repo.delete(product);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return "redirect:/products";
    }
}
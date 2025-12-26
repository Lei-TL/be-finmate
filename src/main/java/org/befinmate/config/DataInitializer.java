package org.befinmate.config;

import lombok.RequiredArgsConstructor;
import org.befinmate.category.repository.CategoryRepository;
import org.befinmate.common.enums.TransactionType;
import org.befinmate.entity.Category;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * ✅ Tự động seed categories khi backend khởi động (nếu chưa có)
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedCategories();
    }

    /**
     * ✅ Seed các categories mặc định nếu chưa có
     */
    private void seedCategories() {
        // Check xem đã có categories chưa
        long count = categoryRepository.count();
        if (count > 0) {
            System.out.println("✅ Categories already exist: " + count + " items. Skipping seed.");
            return;
        }

        System.out.println("🌱 Seeding default categories...");

        // ✅ Danh sách categories mặc định (INCOME)
        List<Category> incomeCategories = Arrays.asList(
                createCategory("Lương", TransactionType.INCOME, "ic_salary", 1),
                createCategory("Thưởng", TransactionType.INCOME, "ic_bonus", 2),
                createCategory("Đầu tư", TransactionType.INCOME, "ic_invest", 3),
                createCategory("Kinh doanh", TransactionType.INCOME, "ic_business", 4),
                createCategory("Cho thuê", TransactionType.INCOME, "ic_rent", 5),
                createCategory("Lãi tiết kiệm", TransactionType.INCOME, "ic_interest", 6),
                createCategory("Quà tặng nhận", TransactionType.INCOME, "ic_gift_received", 7),
                createCategory("Bán hàng", TransactionType.INCOME, "ic_sell", 8),
                createCategory("Hoàn tiền", TransactionType.INCOME, "ic_refund", 9),
                createCategory("Thu nhập khác", TransactionType.INCOME, "ic_other_income", 10)
        );

        // ✅ Danh sách categories mặc định (EXPENSE)
        List<Category> expenseCategories = Arrays.asList(
                createCategory("Ăn uống", TransactionType.EXPENSE, "ic_food", 1),
                createCategory("Mua sắm", TransactionType.EXPENSE, "ic_shopping", 2),
                createCategory("Hóa đơn điện", TransactionType.EXPENSE, "ic_electricbill", 3),
                createCategory("Hóa đơn nước", TransactionType.EXPENSE, "ic_waterbill", 4),
                createCategory("Xăng xe", TransactionType.EXPENSE, "ic_car", 5),
                createCategory("Giải trí", TransactionType.EXPENSE, "ic_entertain", 6),
                createCategory("Sức khỏe", TransactionType.EXPENSE, "ic_health", 7),
                createCategory("Giáo dục", TransactionType.EXPENSE, "ic_education", 8),
                createCategory("Du lịch", TransactionType.EXPENSE, "ic_travel", 9),
                createCategory("Quà tặng", TransactionType.EXPENSE, "ic_gift", 10),
                createCategory("Thời trang", TransactionType.EXPENSE, "ic_fashion", 11),
                createCategory("Hóa đơn", TransactionType.EXPENSE, "ic_bill", 12),
                createCategory("Internet", TransactionType.EXPENSE, "ic_internet", 13),
                createCategory("Điện thoại", TransactionType.EXPENSE, "ic_phone", 14),
                createCategory("Bảo hiểm", TransactionType.EXPENSE, "ic_insurance", 15),
                createCategory("Nhà ở", TransactionType.EXPENSE, "ic_home", 16),
                createCategory("Thú cưng", TransactionType.EXPENSE, "ic_pet", 17),
                createCategory("Thể thao", TransactionType.EXPENSE, "ic_sport", 18),
                createCategory("Đọc sách", TransactionType.EXPENSE, "ic_read", 19),
                createCategory("Khác", TransactionType.EXPENSE, "ic_default_category", 20)
        );

        // ✅ Insert categories
        try {
            categoryRepository.saveAll(incomeCategories);
            categoryRepository.saveAll(expenseCategories);
            System.out.println("✅ Categories seeded successfully: " + (incomeCategories.size() + expenseCategories.size()) + " categories");
        } catch (Exception e) {
            System.err.println("❌ Error seeding categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Category createCategory(String name, TransactionType type, String icon, int displayOrder) {
        Category category = Category.builder()
                .name(name)
                .type(type)
                .icon(icon)
                .displayOrder(displayOrder)
                .build();
        category.setDeleted(false); // ✅ Set deleted từ BaseEntity
        return category;
    }
}


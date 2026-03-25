package com.project.scripts;

import com.project.entity.CategoryTile;
import com.project.entity.HeroBanner;
import com.project.entity.Image;
import com.project.entity.LimitedBanner;
import com.project.entity.LoyaltyPerk;
import com.project.entity.LoyaltySection;
import com.project.repository.CategoryRepository;
import com.project.repository.CategoryTileRepository;
import com.project.repository.HeroBannerRepository;
import com.project.repository.ImageRepository;
import com.project.repository.LimitedBannerRepository;
import com.project.repository.LoyaltySectionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class HomeContentSeeder implements CommandLineRunner {

    private static final String PLACEHOLDER_IMAGE_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9WnW60cAAAAASUVORK5CYII=";

    private final HeroBannerRepository heroBannerRepository;
    private final CategoryTileRepository categoryTileRepository;
    private final LimitedBannerRepository limitedBannerRepository;
    private final LoyaltySectionRepository loyaltySectionRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;

    public HomeContentSeeder(
            HeroBannerRepository heroBannerRepository,
            CategoryTileRepository categoryTileRepository,
            LimitedBannerRepository limitedBannerRepository,
            LoyaltySectionRepository loyaltySectionRepository,
            CategoryRepository categoryRepository,
            ImageRepository imageRepository
    ) {
        this.heroBannerRepository = heroBannerRepository;
        this.categoryTileRepository = categoryTileRepository;
        this.limitedBannerRepository = limitedBannerRepository;
        this.loyaltySectionRepository = loyaltySectionRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedHeroBannersIfEmpty();
        seedCategoryTilesIfEmpty();
        seedLimitedBannerIfEmpty();
        seedLoyaltySectionIfEmpty();
    }

    private void seedHeroBannersIfEmpty() {
        if (heroBannerRepository.count() > 0) {
            return;
        }

        heroBannerRepository.saveAll(List.of(
                createHeroBanner(
                        "Elite Performance Drop",
                        "Top Notch Pro Kit",
                        "Train harder with premium gear designed for speed, stability, and everyday confidence.",
                        "Shop Performance",
                        "/products"
                ),
                createHeroBanner(
                        "New Season Essentials",
                        "Move Bold\nStay Ready",
                        "Fresh apparel, footwear, and training essentials curated for the new season.",
                        "Explore Collection",
                        "/products?sort=newest"
                ),
                createHeroBanner(
                        "Limited Release",
                        "Built For\nTop Sellers",
                        "Discover the highest-rated pieces customers keep coming back for every week.",
                        "See Top Sellers",
                        "/products?sort=popular"
                )
        ));
    }

    private void seedCategoryTilesIfEmpty() {
        if (categoryTileRepository.count() > 0) {
            return;
        }

        categoryTileRepository.saveAll(List.of(
                createCategoryTile("Footwear"),
                createCategoryTile("Apparel"),
                createCategoryTile("Accessories"),
                createCategoryTile("Equipment")
        ));
    }

    private void seedLimitedBannerIfEmpty() {
        if (limitedBannerRepository.count() > 0) {
            return;
        }

        LimitedBanner banner = new LimitedBanner();
        banner.setImage(saveImage("limited-banner.jpg"));
        banner.setBadge("Limited Edition");
        banner.setTitle("Top Notch\nSignature Drop");
        banner.setDescription("A premium capsule release with limited quantities, elevated materials, and bold athletic styling.");
        banner.setCta("Shop Limited");
        banner.setLink("/products");
        limitedBannerRepository.save(banner);
    }

    private void seedLoyaltySectionIfEmpty() {
        if (loyaltySectionRepository.count() > 0) {
            return;
        }

        LoyaltySection section = new LoyaltySection();
        section.setHeading("Join The Top Notch Club");
        section.setDescription("Unlock rewards, early access, and exclusive member-only perks every time you shop.");
        section.setButtonText("Join Loyalty Program");
        section.setPerks(List.of(
                createPerk("Zap", "Fast Rewards", "Earn points with every purchase and redeem them on your next order."),
                createPerk("Gift", "Member Exclusives", "Get early access to drops, bundles, and limited-edition offers."),
                createPerk("Award", "VIP Benefits", "Receive premium support and special bonuses as your membership grows.")
        ));
        loyaltySectionRepository.save(section);
    }

    private HeroBanner createHeroBanner(String subtitle, String title, String description, String cta, String link) {
        HeroBanner banner = new HeroBanner();
        banner.setImages(List.of(saveImage(title.toLowerCase().replaceAll("[^a-z0-9]+", "-") + ".jpg")));
        banner.setSubtitle(subtitle);
        banner.setTitle(title);
        banner.setDescription(description);
        banner.setCta(cta);
        banner.setLink(link);
        return banner;
    }

    private CategoryTile createCategoryTile(String categoryName) {
        CategoryTile tile = new CategoryTile();
        tile.setName(categoryName);
        tile.setLink(resolveCategoryLink(categoryName));
        tile.setImage(saveImage(categoryName.toLowerCase() + "-tile.jpg"));
        return tile;
    }

    private LoyaltyPerk createPerk(String icon, String title, String description) {
        LoyaltyPerk perk = new LoyaltyPerk();
        perk.setIcon(icon);
        perk.setTitle(title);
        perk.setDescription(description);
        return perk;
    }

    private String resolveCategoryLink(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .map(category -> "/products?category=" + category.getId())
                .orElse("/products");
    }

    private Image saveImage(String filename) {
        Image image = new Image();
        image.setFilename(filename);
        image.setContentType("image/png");
        image.setData(java.util.Base64.getDecoder().decode(PLACEHOLDER_IMAGE_BASE64));
        return imageRepository.save(image);
    }
}
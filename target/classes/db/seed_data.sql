-- ============================================================
--  Smart Cafe v2 — Realistic Seed Data
--  Run after schema.sql and upgrade_v2.sql
--  All inserts use INSERT IGNORE to be idempotent
-- ============================================================

USE smart_cafe_db;

-- ── CATEGORIES (total 20) ────────────────────────────────────────────────────
-- 6 already exist from schema.sql; add 14 more
INSERT IGNORE INTO categories (name, description, sort_order) VALUES
    ('Espresso Drinks',      'Shots, americanos, lattes, cappuccinos',        7),
    ('Frappuccinos',         'Blended iced coffee and cream drinks',          8),
    ('Tea & Infusions',      'Green, black, herbal, and bubble teas',         9),
    ('Fresh Juices',         'Cold-pressed and freshly squeezed juices',     10),
    ('Milkshakes',           'Classic and gourmet thick shakes',             11),
    ('Breakfast Items',      'Egg dishes, toasts, and morning specials',     12),
    ('Rice Meals',           'Filipino-inspired rice toppings and combos',   13),
    ('Noodle Dishes',        'Pasta, ramen, and pancit',                     14),
    ('Salads & Wraps',       'Light and healthy options',                    15),
    ('Pizza & Flatbreads',   'Personal-size pizzas and flatbread bites',     16),
    ('Cakes & Slices',       'Celebration cakes, cheesecakes, slices',      17),
    ('Waffles & Pancakes',   'Belgian waffles, fluffy pancakes',             18),
    ('Frozen Desserts',      'Ice cream, gelato, frozen yogurt',             19),
    ('Kids Menu',            'Child-friendly bites and drinks',              20);

-- ── SUPPLIERS (8 suppliers) ──────────────────────────────────────────────────
INSERT IGNORE INTO suppliers (name, contact, phone, email, address) VALUES
    ('Highlands Coffee Beans',  'Carlos Reyes',    '09171001001', 'carlos@highlandsbeans.ph',  'Benguet, Philippines'),
    ('FreshFarm Produce',       'Maria Santos',    '09171001002', 'maria@freshfarm.ph',        'Nueva Ecija, Philippines'),
    ('Dairy Best PH',           'Jose Lim',        '09171001003', 'jose@dairybest.ph',         'Quezon City, Philippines'),
    ('Golden Wheat Bakery',     'Ana Cruz',        '09171001004', 'ana@goldenwheat.ph',        'Bulacan, Philippines'),
    ('Island Sugar Co.',        'Pedro Ramos',     '09171001005', 'pedro@islandsugar.ph',      'Batangas, Philippines'),
    ('Premium Meats PH',        'Sofia Garcia',    '09171001006', 'sofia@premiummeats.ph',     'Pampanga, Philippines'),
    ('Tropical Fruits Hub',     'Mark Dela Cruz',  '09171001007', 'mark@tropicalfruits.ph',    'Cebu, Philippines'),
    ('Kitchen Essentials PH',   'Luna Fernandez',  '09171001008', 'luna@kitchenessentials.ph', 'Manila, Philippines');

-- ── INVENTORY ITEMS (25 items) ───────────────────────────────────────────────
INSERT IGNORE INTO inventory_items (name, unit, current_stock, min_stock, cost_per_unit, supplier_id) VALUES
    ('Arabica Coffee Beans',    'kg',     45.00,  10.00, 380.00, 1),
    ('Robusta Coffee Beans',    'kg',     30.00,   8.00, 280.00, 1),
    ('Whole Milk',              'litre',  60.00,  15.00,  75.00, 3),
    ('Skim Milk',               'litre',  25.00,  10.00,  80.00, 3),
    ('Oat Milk',                'litre',  20.00,   8.00, 120.00, 3),
    ('Heavy Cream',             'litre',  18.00,   5.00, 140.00, 3),
    ('White Sugar',             'kg',     50.00,  15.00,  55.00, 5),
    ('Brown Sugar',             'kg',     30.00,  10.00,  65.00, 5),
    ('All-Purpose Flour',       'kg',     40.00,  10.00,  48.00, 4),
    ('Bread Slices',            'piece', 200.00,  50.00,   8.00, 4),
    ('Croissant Dough',         'piece',  80.00,  20.00,  35.00, 4),
    ('Chocolate Powder',        'kg',     12.00,   3.00, 320.00, 8),
    ('Vanilla Syrup',           'litre',   8.00,   2.00, 280.00, 8),
    ('Caramel Syrup',           'litre',   7.00,   2.00, 290.00, 8),
    ('Matcha Powder',           'kg',      6.00,   2.00, 650.00, 8),
    ('Chicken Breast',          'kg',     25.00,   8.00, 280.00, 6),
    ('Ground Beef',             'kg',     20.00,   6.00, 360.00, 6),
    ('Eggs',                    'piece', 300.00,  60.00,  12.00, 2),
    ('Tomatoes',                'kg',     15.00,   5.00,  55.00, 2),
    ('Lettuce',                 'kg',      8.00,   3.00,  60.00, 2),
    ('Mozzarella Cheese',       'kg',     10.00,   3.00, 420.00, 3),
    ('Whipped Cream',           'can',    24.00,   6.00,  95.00, 3),
    ('Tea Bags Assorted',       'piece', 500.00, 100.00,   6.00, 8),
    ('Boba Pearls',             'kg',     15.00,   4.00, 180.00, 8),
    ('Ice Cream (Vanilla)',     'litre',  20.00,   5.00, 220.00, 3);

-- ── PRODUCTS / MENU ITEMS (100 items) ────────────────────────────────────────
-- Hot Beverages (cat 1)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(1, 'Classic Brewed Coffee',    'House blend, dark roast, served hot',                   85.00,  22.00),
(1, 'Café Americano',           'Two espresso shots with hot water',                    110.00,  28.00),
(1, 'Hot Chocolate Supreme',    'Rich Belgian chocolate with steamed milk',             135.00,  38.00),
(1, 'Chamomile Honey Tea',      'Calming chamomile with local honey',                   95.00,  20.00),
(1, 'Earl Grey Classic',        'Premium Earl Grey steeped 4 minutes',                  90.00,  18.00);

-- Cold Beverages (cat 2)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(2, 'Iced Lemon Tea',           'Freshly brewed tea over ice with lemon slices',        95.00,  22.00),
(2, 'Iced Brown Sugar Latte',   'Espresso with brown sugar oat milk over ice',         155.00,  45.00),
(2, 'Taro Milk Tea',            'Creamy taro with milk tea, shaken with ice',          130.00,  38.00),
(2, 'Cucumber Mint Cooler',     'Fresh cucumber, mint, lemon soda',                   110.00,  28.00),
(2, 'Sparkling Calamansi',      'Fizzy calamansi with honey and basil seeds',          105.00,  25.00);

-- Pastries (cat 3)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(3, 'Butter Croissant',         'Flaky, golden, made fresh every morning',             85.00,  30.00),
(3, 'Blueberry Muffin',         'Bursting with blueberries, topped with streusel',     90.00,  28.00),
(3, 'Cinnamon Danish',          'Swirled cinnamon with cream cheese glaze',            95.00,  32.00),
(3, 'Chocolate Éclair',         'Choux filled with custard, topped with ganache',     110.00,  40.00),
(3, 'Almond Croissant',         'Twice-baked with frangipane and flaked almonds',     110.00,  42.00);

-- Main Courses (cat 4)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(4, 'Club Sandwich',            'Triple-decker with chicken, bacon, egg, lettuce',    195.00,  75.00),
(4, 'Tuna Melt Panini',         'Chunky tuna with cheddar on toasted ciabatta',       185.00,  68.00),
(4, 'BLT Wrap',                 'Bacon, lettuce, tomato in garlic herb wrap',         175.00,  65.00),
(4, 'Grilled Chicken Sandwich', 'Juicy grilled chicken with coleslaw and mustard',    210.00,  80.00),
(4, 'Veggie Burger',            'Black bean patty with avocado and pico de gallo',    185.00,  62.00);

-- Desserts (cat 5)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(5, 'New York Cheesecake',      'Classic New York style with berry compote',           145.00,  55.00),
(5, 'Tiramisu',                 'Italian classic with espresso-soaked ladyfingers',   155.00,  58.00),
(5, 'Chocolate Lava Cake',      'Warm chocolate cake with molten center and ice cream',165.00,  60.00),
(5, 'Leche Flan',               'Filipino custard pudding with caramel sauce',        120.00,  38.00),
(5, 'Crème Brûlée',             'Classic vanilla custard with caramelised sugar top', 145.00,  52.00);

-- Snacks (cat 6)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(6, 'Truffle Fries',            'Crispy fries with truffle oil and parmesan',         145.00,  48.00),
(6, 'Nachos Supreme',           'Corn chips with cheese sauce, jalapeño, sour cream', 165.00,  55.00),
(6, 'Bruschetta Trio',          'Three crostini: tomato basil, mushroom, olive tapenade', 155.00, 52.00),
(6, 'Chicken Wings (6 pcs)',    'Crispy wings with choice of honey garlic or buffalo', 195.00, 65.00),
(6, 'Cheese Quesadilla',        'Flour tortilla with melted cheese and salsa',        155.00,  48.00);

-- Espresso Drinks (cat 7)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(7, 'Espresso Solo',            'Single shot of premium arabica espresso',             85.00,  20.00),
(7, 'Flat White',               'Microfoam milk over double ristretto',               130.00,  38.00),
(7, 'Cappuccino',               'Equal parts espresso, steamed milk, and foam',       125.00,  36.00),
(7, 'Cortado',                  'Espresso cut with equal parts warm milk',            115.00,  32.00),
(7, 'Caramel Macchiato',        'Vanilla latte topped with caramel drizzle',         145.00,  45.00);

-- Frappuccinos (cat 8)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(8, 'Mocha Frappuccino',        'Coffee blended with chocolate and ice, topped with cream', 165.00, 55.00),
(8, 'Caramel Ribbon Crunch',    'Caramel coffee frappé with dark caramel sauce',     175.00,  58.00),
(8, 'Matcha Green Tea Frappé',  'Premium matcha blended with milk and ice',          165.00,  58.00),
(8, 'Strawberry Frappuccino',   'Strawberry blended with ice and cream — no coffee', 155.00,  52.00),
(8, 'Vanilla Bean Frappé',      'Pure vanilla bean cream blended smooth',             155.00,  50.00);

-- Tea & Infusions (cat 9)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(9, 'Brown Sugar Bubble Tea',   'Milk tea with tiger sugar swirl and fresh boba',    145.00,  45.00),
(9, 'Taro Bubble Tea',          'Purple taro milk tea with chewy boba pearls',       145.00,  45.00),
(9, 'Matcha Latte',             'Ceremonial grade matcha with steamed milk',         145.00,  48.00),
(9, 'Honey Lemon Green Tea',    'Premium sencha with honey and fresh lemon',         110.00,  28.00),
(9, 'Jasmine Milk Tea',         'Jasmine-scented tea with creamy milk',              120.00,  35.00);

-- Fresh Juices (cat 10)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(10, 'Orange Juice',            'Freshly squeezed Valencia oranges',                  105.00,  32.00),
(10, 'Watermelon Juice',        'Cold-pressed watermelon, no added sugar',            100.00,  28.00),
(10, 'Green Detox Juice',       'Spinach, cucumber, celery, apple, ginger',          125.00,  42.00),
(10, 'Mango Pineapple Juice',   'Tropical blend of sweet mango and pineapple',       115.00,  35.00),
(10, 'Beet & Carrot Juice',     'Earthy beet with sweet carrot and orange',          120.00,  38.00);

-- Milkshakes (cat 11)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(11, 'Classic Chocolate Shake', 'Rich chocolate ice cream blended with milk',        165.00,  55.00),
(11, 'Salted Caramel Shake',    'Caramel ice cream with a pinch of sea salt',        175.00,  58.00),
(11, 'Strawberry Cheesecake Shake', 'Strawberry ice cream with real graham crumble', 175.00, 60.00),
(11, 'Ube Milkshake',           'Filipino purple yam ice cream shake',               165.00,  58.00),
(11, 'Cookies & Cream Shake',   'Oreo blended into vanilla ice cream',               170.00,  58.00);

-- Breakfast Items (cat 12)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(12, 'Full Cafe Breakfast',     'Two eggs, bacon, toast, hash brown, grilled tomato', 295.00, 115.00),
(12, 'Avocado Toast',           'Sourdough with smashed avocado, poached egg, chili', 215.00,  80.00),
(12, 'Eggs Benedict',           'Poached eggs, Canadian bacon on muffin, hollandaise', 265.00, 105.00),
(12, 'Breakfast Burrito',       'Scrambled eggs, cheese, chorizo in flour tortilla', 225.00,  85.00),
(12, 'French Toast Stack',      'Brioche dipped in cinnamon egg batter with maple',  185.00,  65.00);

-- Rice Meals (cat 13)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(13, 'Silog Meals (Chicken)',   'Chicken inasal with garlic rice and sunny-side egg', 195.00,  75.00),
(13, 'Beef Tapa Rice',          'Sweet cured beef with garlic fried rice and egg',   205.00,  80.00),
(13, 'Pork Tocino Rice',        'Sweet cured pork with rice, egg, and atchara',      195.00,  72.00),
(13, 'Longsilog',               'Longganisa with garlic rice and fried egg',          185.00,  68.00),
(13, 'Bangsilog',               'Boneless bangus with rice, egg, and tomato',        205.00,  78.00);

-- Noodle Dishes (cat 14)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(14, 'Creamy Carbonara',        'Spaghetti in rich cream sauce with bacon and mushrooms', 215.00, 78.00),
(14, 'Beef Ramen',              'Slow-braised beef in rich soy broth with ramen noodles', 245.00, 95.00),
(14, 'Pancit Canton',           'Stir-fried egg noodles with pork, shrimp, and veggies', 195.00, 72.00),
(14, 'Pesto Pasta',             'Al dente pasta in homemade basil pesto with pine nuts', 215.00, 78.00),
(14, 'Laksa Noodle Soup',       'Malaysian coconut curry soup with tofu and noodles', 235.00, 88.00);

-- Salads & Wraps (cat 15)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(15, 'Caesar Salad',            'Romaine, parmesan, croutons, Caesar dressing',      175.00,  58.00),
(15, 'Greek Salad',             'Cucumber, olives, feta, tomatoes, red onion',       185.00,  62.00),
(15, 'Grilled Chicken Wrap',    'Grilled chicken with greens and honey mustard',     195.00,  72.00),
(15, 'Falafel Wrap',            'Crispy falafel with tahini and roasted vegetables', 185.00,  65.00),
(15, 'Asian Noodle Salad',      'Glass noodles, edamame, sesame ginger dressing',   175.00,  60.00);

-- Pizza & Flatbreads (cat 16)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(16, 'Margherita Flatbread',    'Classic tomato, fresh mozzarella, basil',           195.00,  68.00),
(16, 'BBQ Chicken Flatbread',   'Smoky BBQ sauce, grilled chicken, red onion',       215.00,  78.00),
(16, 'Pepperoni Personal Pizza','Loaded pepperoni on hand-tossed crust',             235.00,  85.00),
(16, 'Four Cheese Flatbread',   'Mozzarella, cheddar, parmesan, gorgonzola',        225.00,  82.00),
(16, 'Pesto Veggie Flatbread',  'Pesto base with zucchini, bell pepper, olives',    205.00,  72.00);

-- Cakes & Slices (cat 17)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(17, 'Triple Chocolate Cake',   'Dark, milk, and white chocolate layers',            165.00,  60.00),
(17, 'Mango Cheesecake',        'No-bake mango cheesecake with graham crust',        155.00,  55.00),
(17, 'Ube Chiffon Cake',        'Light purple yam chiffon with coconut frosting',   145.00,  50.00),
(17, 'Red Velvet Slice',        'Classic red velvet with cream cheese frosting',     155.00,  55.00),
(17, 'Matcha Opera Cake',       'Layered matcha sponge with white chocolate ganache', 165.00, 62.00);

-- Waffles & Pancakes (cat 18)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(18, 'Belgian Waffle Classic',  'Golden waffle with maple syrup, butter, cream',    165.00,  55.00),
(18, 'Ube Waffle',              'Purple yam waffle topped with ube ice cream',      185.00,  68.00),
(18, 'Buttermilk Pancake Stack','Fluffy stack with blueberry compote and cream',    175.00,  60.00),
(18, 'Nutella Waffle',          'Crispy waffle smothered in Nutella and banana',    185.00,  65.00),
(18, 'Banana Foster Pancakes',  'Pancakes flambéed with banana in rum caramel',     195.00,  72.00);

-- Frozen Desserts (cat 19)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(19, 'Double Scoop Ice Cream',  'Choice of 2 scoops from 12 flavours',              105.00,  35.00),
(19, 'Sundae Supreme',          'Three scoops with hot fudge, nuts, and cherry',    145.00,  52.00),
(19, 'Gelato Flight',           'Trio of Italian gelato scoops',                    165.00,  60.00),
(19, 'Mango Sorbet',            'Refreshing tropical mango sorbet, dairy-free',     120.00,  40.00),
(19, 'Frozen Yogurt Parfait',   'Tart froyo with granola and seasonal berries',     135.00,  45.00);

-- Kids Menu (cat 20)
INSERT IGNORE INTO menu_items (category_id, name, description, price, cost_price) VALUES
(20, 'Mini Pancakes (4 pcs)',   'Bite-sized pancakes with strawberry jam and cream', 115.00,  38.00),
(20, 'Grilled Cheese Soldiers', 'Toasted cheese fingers with tomato soup dip',      125.00,  42.00),
(20, 'Kids Hot Chocolate',      'Creamy hot choc with mini marshmallows',            85.00,  25.00),
(20, 'Mini Pizza',              'Kids cheese pizza with tomato and mozzarella',     135.00,  45.00),
(20, 'Fruit Platter',           'Seasonal fresh fruit with honey yogurt dip',       110.00,  38.00);

-- ── USERS (additional staff accounts) ────────────────────────────────────────
-- BCrypt hash for 'staff123'
INSERT IGNORE INTO users (full_name, username, email, password_hash, phone, role, is_active) VALUES
('Maria Santos',    'mariasantos',  'maria@smartcafe.ph',   '$2a$10$lp0MmVX32.RsBO9bxGqYuevmX4/1Z20SFkh0jpqWZwAXIC4Vot0Sy', '09171001101', 'CASHIER',       TRUE),
('Juan Dela Cruz',  'juandeluz',    'juan@smartcafe.ph',    '$2a$10$lp0MmVX32.RsBO9bxGqYuevmX4/1Z20SFkh0jpqWZwAXIC4Vot0Sy', '09171001102', 'CASHIER',       TRUE),
('Ana Reyes',       'anareyes',     'ana@smartcafe.ph',     '$2a$10$lp0MmVX32.RsBO9bxGqYuevmX4/1Z20SFkh0jpqWZwAXIC4Vot0Sy', '09171001103', 'KITCHEN_STAFF', TRUE),
('Pedro Garcia',    'pedrogarcia',  'pedro@smartcafe.ph',   '$2a$10$lp0MmVX32.RsBO9bxGqYuevmX4/1Z20SFkh0jpqWZwAXIC4Vot0Sy', '09171001104', 'KITCHEN_STAFF', TRUE),
('Sofia Lim',       'sofialim',     'sofia@smartcafe.ph',   '$2a$10$lp0MmVX32.RsBO9bxGqYuevmX4/1Z20SFkh0jpqWZwAXIC4Vot0Sy', '09171001105', 'MANAGER',       TRUE);

-- ── EMPLOYEES ─────────────────────────────────────────────────────────────────
INSERT IGNORE INTO employees (full_name, phone, email, position, department, base_salary, hire_date, is_active) VALUES
('Maria Santos',    '09171001101', 'maria@smartcafe.ph',   'Senior Cashier',    'Front of House', 22000.00, '2023-01-15', TRUE),
('Juan Dela Cruz',  '09171001102', 'juan@smartcafe.ph',    'Cashier',           'Front of House', 18000.00, '2023-06-01', TRUE),
('Ana Reyes',       '09171001103', 'ana@smartcafe.ph',     'Head Chef',         'Kitchen',        28000.00, '2022-08-10', TRUE),
('Pedro Garcia',    '09171001104', 'pedro@smartcafe.ph',   'Line Cook',         'Kitchen',        20000.00, '2023-03-20', TRUE),
('Sofia Lim',       '09171001105', 'sofia@smartcafe.ph',   'Shift Manager',     'Management',     32000.00, '2022-05-01', TRUE),
('Carlos Mendoza',  '09171001106', 'carlos@smartcafe.ph',  'Barista',           'Bar',            19000.00, '2023-09-15', TRUE),
('Lisa Tan',        '09171001107', 'lisa@smartcafe.ph',    'Barista',           'Bar',            19000.00, '2024-01-10', TRUE),
('Ryan Ocampo',     '09171001108', 'ryan@smartcafe.ph',    'Waiter',            'Front of House', 16000.00, '2024-02-01', TRUE),
('Grace Villanueva','09171001109', 'grace@smartcafe.ph',   'Waitress',          'Front of House', 16000.00, '2024-02-01', TRUE),
('Bong Pascual',    '09171001110', 'bong@smartcafe.ph',    'Kitchen Helper',    'Kitchen',        15000.00, '2024-03-15', TRUE);

-- ── CUSTOMERS (30 customers) ─────────────────────────────────────────────────
INSERT IGNORE INTO customers (full_name, phone, email, loyalty_points, total_spent, visit_count) VALUES
('Alejandro Reyes',   '09170001001', 'alex.reyes@gmail.com',    850,  12450.00, 42),
('Beatriz Santos',    '09170001002', 'bea.santos@gmail.com',    620,   9800.00, 31),
('Carlo Mendoza',     '09170001003', 'carlo.m@yahoo.com',       420,   7200.00, 24),
('Diana Cruz',        '09170001004', 'diana.cruz@gmail.com',   1250,  18500.00, 58),
('Eduardo Lim',       '09170001005', 'eduardo.lim@hotmail.com', 380,   5800.00, 19),
('Fernanda Garcia',   '09170001006', 'ferna.g@gmail.com',       750,  11200.00, 37),
('Gerard Tan',        '09170001007', 'gerard.tan@gmail.com',   1800,  24500.00, 72),
('Helen Ong',         '09170001008', 'helen.ong@yahoo.com',     560,   8400.00, 28),
('Ivan Villanueva',   '09170001009', 'ivan.v@gmail.com',        290,   4500.00, 15),
('Jessica Pascual',   '09170001010', 'jess.p@gmail.com',        920,  13800.00, 46),
('Kevin Soriano',     '09170001011', 'kevin.s@gmail.com',       410,   6300.00, 21),
('Laura Bautista',    '09170001012', 'laura.b@yahoo.com',       680,  10200.00, 34),
('Miguel Torres',     '09170001013', 'miguel.t@gmail.com',     2100,  28000.00, 85),
('Nina Ramos',        '09170001014', 'nina.r@gmail.com',        340,   5100.00, 17),
('Oscar Dela Cruz',   '09170001015', 'oscar.dc@hotmail.com',    780,  11700.00, 39),
('Patricia Reyes',    '09170001016', 'patty.r@gmail.com',       530,   7950.00, 26),
('Quincy Ocampo',     '09170001017', 'quincy.o@gmail.com',      160,   2400.00,  8),
('Rachel Fernandez',  '09170001018', 'rachel.f@yahoo.com',      890,  13350.00, 44),
('Samuel Magpayo',    '09170001019', 'samuel.m@gmail.com',      470,   7050.00, 23),
('Theresa Aguilar',   '09170001020', 'theresa.a@gmail.com',    1050,  15750.00, 52),
('Ulysses Navarro',   '09170001021', 'ulysses.n@gmail.com',     720,  10800.00, 36),
('Vanessa Castillo',  '09170001022', 'vanessa.c@yahoo.com',     290,   4350.00, 14),
('William Dizon',     '09170001023', 'will.d@gmail.com',        830,  12450.00, 41),
('Ximena Aquino',     '09170001024', 'ximena.a@gmail.com',      450,   6750.00, 22),
('Yolanda Tolentino', '09170001025', 'yolanda.t@hotmail.com',   610,   9150.00, 30),
('Zachary Panganiban','09170001026', 'zach.p@gmail.com',        380,   5700.00, 19),
('Abigail Valdez',    '09170001027', 'abby.v@gmail.com',       1350,  20250.00, 67),
('Benjamin Salazar',  '09170001028', 'ben.s@yahoo.com',         490,   7350.00, 24),
('Camille Montoya',   '09170001029', 'camille.m@gmail.com',     710,  10650.00, 35),
('Daniel Espinoza',   '09170001030', 'dan.e@gmail.com',         580,   8700.00, 29);

-- ── RESERVATIONS (15 reservations spread across today + next 7 days) ──────────
INSERT IGNORE INTO reservations (table_id, customer_id, customer_name, party_size, reservation_date, reservation_time, status, notes) VALUES
(3, 1,  'Alejandro Reyes',   4, CURDATE(),              '12:00:00', 'CONFIRMED', 'Anniversary dinner'),
(5, 4,  'Diana Cruz',        6, CURDATE(),              '18:30:00', 'CONFIRMED', 'Birthday celebration'),
(7, 7,  'Gerard Tan',        4, CURDATE(),              '14:00:00', 'PENDING',   'Business lunch'),
(9, 13, 'Miguel Torres',     8, CURDATE(),              '19:00:00', 'CONFIRMED', 'Team outing'),
(2, 10, 'Jessica Pascual',   2, DATE_ADD(CURDATE(),INTERVAL 1 DAY), '13:00:00', 'PENDING', 'Date night'),
(4, 20, 'Theresa Aguilar',   4, DATE_ADD(CURDATE(),INTERVAL 1 DAY), '18:00:00', 'CONFIRMED', NULL),
(6, 27, 'Abigail Valdez',    5, DATE_ADD(CURDATE(),INTERVAL 2 DAY), '12:30:00', 'PENDING', 'Baby shower'),
(1, 2,  'Beatriz Santos',    2, DATE_ADD(CURDATE(),INTERVAL 2 DAY), '11:00:00', 'CONFIRMED', NULL),
(8, 15, 'Oscar Dela Cruz',   4, DATE_ADD(CURDATE(),INTERVAL 3 DAY), '19:30:00', 'PENDING', NULL),
(3, 22, 'Vanessa Castillo',  3, DATE_ADD(CURDATE(),INTERVAL 3 DAY), '13:00:00', 'CONFIRMED', NULL),
(5, 6,  'Fernanda Garcia',   6, DATE_ADD(CURDATE(),INTERVAL 4 DAY), '18:00:00', 'PENDING', 'Farewell party'),
(2, 29, 'Camille Montoya',   2, DATE_ADD(CURDATE(),INTERVAL 5 DAY), '12:00:00', 'CONFIRMED', NULL),
(7, 11, 'Kevin Soriano',     4, DATE_ADD(CURDATE(),INTERVAL 5 DAY), '20:00:00', 'PENDING', NULL),
(4, 8,  'Helen Ong',         3, DATE_ADD(CURDATE(),INTERVAL 6 DAY), '13:30:00', 'CONFIRMED', NULL),
(6, 16, 'Patricia Reyes',    4, DATE_ADD(CURDATE(),INTERVAL 7 DAY), '18:30:00', 'PENDING', NULL);

-- ── ORDERS + ORDER_ITEMS + PAYMENTS (historical + active) ────────────────────
-- Historical completed orders (past 30 days)
INSERT IGNORE INTO orders
    (order_number, table_id, cashier_id, customer_id, customer_name, order_type, status, subtotal, tax, discount, total, created_at)
VALUES
('ORD-H-001', 3, 1, 1,  'Alejandro Reyes',  'DINE_IN',  'COMPLETED', 435.00, 52.20,  0.00, 487.20, DATE_SUB(NOW(), INTERVAL 29 DAY)),
('ORD-H-002', 5, 1, 4,  'Diana Cruz',       'DINE_IN',  'COMPLETED', 695.00, 83.40,  0.00, 778.40, DATE_SUB(NOW(), INTERVAL 28 DAY)),
('ORD-H-003', NULL, 1, 7, 'Gerard Tan',     'TAKEAWAY', 'COMPLETED', 320.00, 38.40,  0.00, 358.40, DATE_SUB(NOW(), INTERVAL 27 DAY)),
('ORD-H-004', 2, 1, 13, 'Miguel Torres',    'DINE_IN',  'COMPLETED', 875.00,105.00,  0.00, 980.00, DATE_SUB(NOW(), INTERVAL 26 DAY)),
('ORD-H-005', 4, 1, 10, 'Jessica Pascual',  'DINE_IN',  'COMPLETED', 290.00, 34.80,  0.00, 324.80, DATE_SUB(NOW(), INTERVAL 25 DAY)),
('ORD-H-006', NULL, 1, 2, 'Beatriz Santos', 'TAKEAWAY', 'COMPLETED', 185.00, 22.20,  0.00, 207.20, DATE_SUB(NOW(), INTERVAL 24 DAY)),
('ORD-H-007', 3, 1, 20, 'Theresa Aguilar', 'DINE_IN',  'COMPLETED', 560.00, 67.20,  0.00, 627.20, DATE_SUB(NOW(), INTERVAL 23 DAY)),
('ORD-H-008', 7, 1, 27, 'Abigail Valdez',  'DINE_IN',  'COMPLETED', 920.00,110.40,  0.00,1030.40, DATE_SUB(NOW(), INTERVAL 22 DAY)),
('ORD-H-009', NULL, 1, 6, 'Fernanda Garcia','TAKEAWAY', 'COMPLETED', 270.00, 32.40,  0.00, 302.40, DATE_SUB(NOW(), INTERVAL 21 DAY)),
('ORD-H-010', 5, 1, 15, 'Oscar Dela Cruz', 'DINE_IN',  'COMPLETED', 485.00, 58.20,  0.00, 543.20, DATE_SUB(NOW(), INTERVAL 20 DAY)),
('ORD-H-011', 2, 1, 8,  'Helen Ong',       'DINE_IN',  'COMPLETED', 340.00, 40.80,  0.00, 380.80, DATE_SUB(NOW(), INTERVAL 19 DAY)),
('ORD-H-012', NULL, 1, 11,'Kevin Soriano',  'TAKEAWAY', 'COMPLETED', 220.00, 26.40,  0.00, 246.40, DATE_SUB(NOW(), INTERVAL 18 DAY)),
('ORD-H-013', 3, 1, 29, 'Camille Montoya', 'DINE_IN',  'COMPLETED', 610.00, 73.20,  0.00, 683.20, DATE_SUB(NOW(), INTERVAL 17 DAY)),
('ORD-H-014', 6, 1, 3,  'Carlo Mendoza',   'DINE_IN',  'COMPLETED', 395.00, 47.40,  0.00, 442.40, DATE_SUB(NOW(), INTERVAL 16 DAY)),
('ORD-H-015', NULL, 1, 18,'Rachel Fernandez','DELIVERY','COMPLETED', 450.00, 54.00,  0.00, 504.00, DATE_SUB(NOW(), INTERVAL 15 DAY)),
('ORD-H-016', 4, 1, 21, 'Ulysses Navarro', 'DINE_IN',  'COMPLETED', 530.00, 63.60,  0.00, 593.60, DATE_SUB(NOW(), INTERVAL 14 DAY)),
('ORD-H-017', NULL, 1, 5, 'Eduardo Lim',   'TAKEAWAY', 'COMPLETED', 165.00, 19.80,  0.00, 184.80, DATE_SUB(NOW(), INTERVAL 13 DAY)),
('ORD-H-018', 7, 1, 12, 'Laura Bautista',  'DINE_IN',  'COMPLETED', 720.00, 86.40,  0.00, 806.40, DATE_SUB(NOW(), INTERVAL 12 DAY)),
('ORD-H-019', 3, 1, 24, 'Ximena Aquino',   'DINE_IN',  'COMPLETED', 380.00, 45.60,  0.00, 425.60, DATE_SUB(NOW(), INTERVAL 11 DAY)),
('ORD-H-020', NULL, 1, 9, 'Ivan Villanueva','TAKEAWAY', 'COMPLETED', 245.00, 29.40,  0.00, 274.40, DATE_SUB(NOW(), INTERVAL 10 DAY)),
('ORD-H-021', 5, 1, 30, 'Daniel Espinoza', 'DINE_IN',  'COMPLETED', 490.00, 58.80,  0.00, 548.80, DATE_SUB(NOW(), INTERVAL 9 DAY)),
('ORD-H-022', 2, 1, 16, 'Patricia Reyes',  'DINE_IN',  'COMPLETED', 335.00, 40.20,  0.00, 375.20, DATE_SUB(NOW(), INTERVAL 8 DAY)),
('ORD-H-023', NULL, 1, 19,'Samuel Magpayo', 'DELIVERY', 'COMPLETED', 580.00, 69.60,  0.00, 649.60, DATE_SUB(NOW(), INTERVAL 7 DAY)),
('ORD-H-024', 4, 1, 26, 'Zachary Panganiban','DINE_IN', 'COMPLETED', 295.00, 35.40,  0.00, 330.40, DATE_SUB(NOW(), INTERVAL 6 DAY)),
('ORD-H-025', 6, 1, 14, 'Nina Ramos',      'DINE_IN',  'COMPLETED', 415.00, 49.80,  0.00, 464.80, DATE_SUB(NOW(), INTERVAL 5 DAY)),
-- Today's completed orders
('ORD-T-001', 3, 1, 1,  'Alejandro Reyes', 'DINE_IN',  'COMPLETED', 520.00, 62.40,  0.00, 582.40, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('ORD-T-002', NULL, 1, 7,'Gerard Tan',     'TAKEAWAY', 'COMPLETED', 295.00, 35.40,  0.00, 330.40, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('ORD-T-003', 5, 1, 13, 'Miguel Torres',   'DINE_IN',  'COMPLETED', 680.00, 81.60,  0.00, 761.60, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('ORD-T-004', 2, 1, 27, 'Abigail Valdez',  'DINE_IN',  'COMPLETED', 445.00, 53.40,  0.00, 498.40, DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
('ORD-T-005', NULL, 1, 4, 'Diana Cruz',    'DELIVERY', 'COMPLETED', 360.00, 43.20,  0.00, 403.20, DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
-- Active orders (different statuses)
('ORD-A-001', 3, 1, 10, 'Jessica Pascual', 'DINE_IN',  'PREPARING', 385.00, 46.20,  0.00, 431.20, DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
('ORD-A-002', 6, 1, 20, 'Theresa Aguilar', 'DINE_IN',  'READY',     290.00, 34.80,  0.00, 324.80, DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
('ORD-A-003', NULL, 1, 2,'Beatriz Santos', 'TAKEAWAY', 'CONFIRMED', 175.00, 21.00,  0.00, 196.00, DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
('ORD-A-004', 7, 1, 15, 'Oscar Dela Cruz', 'DINE_IN',  'PENDING',   480.00, 57.60,  0.00, 537.60, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
('ORD-A-005', 4, 1, 22, 'Vanessa Castillo','DINE_IN',  'NEW',       320.00, 38.40,  0.00, 358.40, NOW());

-- ── ORDER ITEMS ───────────────────────────────────────────────────────────────
-- Link items to the historical orders (just top sellers for realism)
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 31, 2, 130.00, 260.00, 'SERVED'
FROM orders o WHERE o.order_number = 'ORD-H-001';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 1,  1,  85.00,  85.00, 'SERVED'
FROM orders o WHERE o.order_number = 'ORD-H-001';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 11,  1,  85.00,  85.00, 'SERVED'
FROM orders o WHERE o.order_number = 'ORD-H-001';

INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 16, 2, 195.00, 390.00, 'SERVED'
FROM orders o WHERE o.order_number = 'ORD-H-002';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 37, 1, 165.00, 165.00, 'SERVED'
FROM orders o WHERE o.order_number = 'ORD-H-002';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 7,  1, 130.00, 130.00, 'SERVED'
FROM orders o WHERE o.order_number = 'ORD-H-002';

-- Active order items
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 32, 1, 130.00, 130.00, 'READY'
FROM orders o WHERE o.order_number = 'ORD-A-001';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 17, 1, 185.00, 185.00, 'READY'
FROM orders o WHERE o.order_number = 'ORD-A-001';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 21, 1, 145.00, 145.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-002';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 6,  1,  95.00,  95.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-002';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 11, 1,  85.00,  85.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-002';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 33, 1, 125.00, 125.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-003';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 11, 1,  85.00,  85.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-003';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 16, 2, 195.00, 390.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-004';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 26, 1, 145.00, 145.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-004';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 31, 1, 130.00, 130.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-005';
INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price, subtotal, status)
SELECT o.id, 81, 1, 165.00, 165.00, 'PENDING'
FROM orders o WHERE o.order_number = 'ORD-A-005';

-- ── PAYMENTS for completed orders ─────────────────────────────────────────────
INSERT IGNORE INTO payments (receipt_number, order_id, payment_method, amount_paid, change_amount, payment_status, cashier_id, paid_at)
SELECT CONCAT('RCP-', LPAD(o.id, 6, '0')), o.id, 'CASH', o.total + 10, 10.00, 'PAID', 1, o.created_at
FROM orders o WHERE o.status = 'COMPLETED';

-- ── INITIAL NOTIFICATIONS ─────────────────────────────────────────────────────
INSERT IGNORE INTO app_notifications (type, title, message, reference_id, is_read) VALUES
('LOW_STOCK',        'Low Stock Alert',       'Matcha Powder is below minimum stock level (6 kg remaining, min: 2 kg)', 15, FALSE),
('LOW_STOCK',        'Low Stock Alert',       'Vanilla Syrup is running low (8 L remaining, min: 2 L)', 13, FALSE),
('NEW_ORDER',        'New Order Received',    'Order ORD-A-005 placed by Vanessa Castillo — Table 4', NULL, FALSE),
('RESERVATION_TODAY','Reservation Today',     '4 reservations scheduled for today. First at 12:00 PM — Alejandro Reyes (Table 3)', NULL, TRUE);

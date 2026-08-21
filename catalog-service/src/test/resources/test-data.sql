delete from products;

insert into products(code, name, description, image_url, price)
select
    'TEST-' || lpad(product_number::text, 2, '0'),
    'Product ' || lpad(product_number::text, 2, '0'),
    'Test product ' || product_number,
    'https://example.com/products/' || product_number || '.jpg',
    product_number
from generate_series(1, 14) as product_number;

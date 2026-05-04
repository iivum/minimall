-- Add member_grade and total_spent columns to users table
ALTER TABLE users ADD COLUMN member_grade VARCHAR(10) NOT NULL DEFAULT 'L1';
ALTER TABLE users ADD COLUMN total_spent DECIMAL(10, 2) NOT NULL DEFAULT 0;
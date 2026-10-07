-- test users, password for both is Str0ng@Pass (stored as bcrypt hash)
INSERT IGNORE INTO app_user (cif, login_id, password_hash, full_name, email, mobile_no, status, failed_login_count)
VALUES
    ('CIF0001', 'karim.ahmed', '$2a$10$5z8KmjaPRvBD2LnvNiLgV.7.4SZ6304d9wEE07b5C3s3aIaP4MBAu',
     'Karim Ahmed', 'karim@example.com', '01711000001', 'ACTIVE', 0),
    ('CIF0002', 'nusrat.jahan', '$2a$10$5z8KmjaPRvBD2LnvNiLgV.7.4SZ6304d9wEE07b5C3s3aIaP4MBAu',
     'Nusrat Jahan', 'nusrat@example.com', '01711000002', 'ACTIVE', 0);

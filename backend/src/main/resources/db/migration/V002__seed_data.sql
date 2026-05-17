-- UUIDs for users (generated for seed data)
SET @user1_id = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', true);
SET @user2_id = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', true);
SET @user3_id = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003', true);

-- UUIDs for user 1 lists
SET @user1_list1_id = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655450001', true);
SET @user1_list2_id = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655450002', true);
SET @user1_list3_id = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655450003', true);

-- UUIDs for user 2 lists
SET @user2_list1_id = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655450004', true);
SET @user2_list2_id = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655450005', true);

-- Insert sample users (identified by email)
INSERT INTO user (id, email, display_name, avatar_url)
VALUES
    (BIN_TO_UUID(@user1_id, true), 'john.google@example.com', 'John (Google)', 'https://example.com/john-google.jpg'),
    (BIN_TO_UUID(@user2_id, true), 'jane.github@example.com', 'Jane (GitHub)', 'https://example.com/jane-github.jpg'),
    (BIN_TO_UUID(@user3_id, true), 'alex.multi@example.com', 'Alex Multi-Auth', 'https://example.com/alex.jpg');

-- Link OAuth providers to users
INSERT INTO user_auth_provider (id, user_id, provider, provider_user_id)
VALUES
    (UUID(), BIN_TO_UUID(@user1_id, true), 'google', 'google_john_123456789'),
    (UUID(), BIN_TO_UUID(@user2_id, true), 'github', 'github_jane_987654321'),
    (UUID(), BIN_TO_UUID(@user3_id, true), 'google', 'google_alex_111111111'),
    (UUID(), BIN_TO_UUID(@user3_id, true), 'github', 'github_alex_222222222');

-- Insert sample lists for user 1 (John)
INSERT INTO task_list (id, user_id, title)
VALUES
    (BIN_TO_UUID(@user1_list1_id, true), BIN_TO_UUID(@user1_id, true), 'Personal'),
    (BIN_TO_UUID(@user1_list2_id, true), BIN_TO_UUID(@user1_id, true), 'Work'),
    (BIN_TO_UUID(@user1_list3_id, true), BIN_TO_UUID(@user1_id, true), 'Shopping');

-- Insert sample lists for user 2 (Jane)
INSERT INTO task_list (id, user_id, title)
VALUES
    (BIN_TO_UUID(@user2_list1_id, true), BIN_TO_UUID(@user2_id, true), 'Home Projects'),
    (BIN_TO_UUID(@user2_list2_id, true), BIN_TO_UUID(@user2_id, true), 'Learning');

-- Insert sample tasks for user 1 (John)
INSERT INTO task (id, list_id, user_id, title, description, status, priority, due_date)
VALUES
    (UUID(), BIN_TO_UUID(@user1_list1_id, true), BIN_TO_UUID(@user1_id, true), 'Morning walk', 'Take a 30-minute walk around the neighborhood', 'TO_DO', 'LOW', DATE_ADD(CURDATE(), INTERVAL 1 DAY)),
    (UUID(), BIN_TO_UUID(@user1_list1_id, true), BIN_TO_UUID(@user1_id, true), 'Read a book', 'Finish reading chapter 5', 'IN_PROGRESS', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
    (UUID(), BIN_TO_UUID(@user1_list1_id, true), BIN_TO_UUID(@user1_id, true), 'Overdue task', 'This task is past due', 'TO_DO', 'HIGH', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
    (UUID(), BIN_TO_UUID(@user1_list2_id, true), BIN_TO_UUID(@user1_id, true), 'Project review', 'Review Q2 project deliverables', 'TO_DO', 'HIGH', DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
    (UUID(), BIN_TO_UUID(@user1_list2_id, true), BIN_TO_UUID(@user1_id, true), 'Team meeting notes', 'Document action items from team sync', 'DONE', 'MEDIUM', CURDATE()),
    (UUID(), BIN_TO_UUID(@user1_list3_id, true), BIN_TO_UUID(@user1_id, true), 'Groceries', 'Milk, eggs, bread, vegetables', 'TO_DO', 'MEDIUM', CURDATE());

-- Insert sample tasks for user 2 (Jane)
INSERT INTO task (id, list_id, user_id, title, description, status, priority, due_date)
VALUES
    (UUID(), BIN_TO_UUID(@user2_list1_id, true), BIN_TO_UUID(@user2_id, true), 'Paint bedroom', 'Paint master bedroom walls', 'TO_DO', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
    (UUID(), BIN_TO_UUID(@user2_list1_id, true), BIN_TO_UUID(@user2_id, true), 'Fix leaky faucet', 'Kitchen sink faucet needs repair', 'IN_PROGRESS', 'HIGH', DATE_ADD(CURDATE(), INTERVAL 1 DAY)),
    (UUID(), BIN_TO_UUID(@user2_list2_id, true), BIN_TO_UUID(@user2_id, true), 'Learn Docker', 'Complete Docker beginner course', 'TO_DO', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 7 DAY));

-- User 3 (Alex) has no lists/tasks initially

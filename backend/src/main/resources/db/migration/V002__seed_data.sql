-- Insert sample users (identified by email)
INSERT INTO users (email, display_name, avatar_url)
VALUES
    ('john.google@example.com', 'John (Google)', 'https://example.com/john-google.jpg'),
    ('jane.github@example.com', 'Jane (GitHub)', 'https://example.com/jane-github.jpg'),
    ('alex.multi@example.com', 'Alex Multi-Auth', 'https://example.com/alex.jpg');

-- Link OAuth providers to users
INSERT INTO user_auth_providers (user_id, provider, provider_user_id)
VALUES
    (1, 'google', 'google_john_123456789'),
    (2, 'github', 'github_jane_987654321'),
    (3, 'google', 'google_alex_111111111'),
    (3, 'github', 'github_alex_222222222');

-- Insert sample lists for user 1 (John)
INSERT INTO task_lists (user_id, title)
VALUES
    (1, 'Personal'),
    (1, 'Work'),
    (1, 'Shopping');

-- Insert sample lists for user 2 (Jane)
INSERT INTO task_lists (user_id, title)
VALUES
    (2, 'Home Projects'),
    (2, 'Learning');

-- Insert sample tasks for user 1 (John)
INSERT INTO tasks (list_id, user_id, title, description, status, priority, due_date)
VALUES
    (1, 1, 'Morning walk', 'Take a 30-minute walk around the neighborhood', 'TO_DO', 'LOW', DATE_ADD(CURDATE(), INTERVAL 1 DAY)),
    (1, 1, 'Read a book', 'Finish reading chapter 5', 'IN_PROGRESS', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
    (1, 1, 'Overdue task', 'This task is past due', 'TO_DO', 'HIGH', DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
    (2, 1, 'Project review', 'Review Q2 project deliverables', 'TO_DO', 'HIGH', DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
    (2, 1, 'Team meeting notes', 'Document action items from team sync', 'DONE', 'MEDIUM', CURDATE()),
    (3, 1, 'Groceries', 'Milk, eggs, bread, vegetables', 'TO_DO', 'MEDIUM', CURDATE());

-- Insert sample tasks for user 2 (Jane)
INSERT INTO tasks (list_id, user_id, title, description, status, priority, due_date)
VALUES
    (4, 2, 'Paint bedroom', 'Paint master bedroom walls', 'TO_DO', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
    (4, 2, 'Fix leaky faucet', 'Kitchen sink faucet needs repair', 'IN_PROGRESS', 'HIGH', DATE_ADD(CURDATE(), INTERVAL 1 DAY)),
    (5, 2, 'Learn Docker', 'Complete Docker beginner course', 'TO_DO', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 7 DAY));

-- Insert sample tasks for user 3 (Alex - multi-auth user)
-- User 3 has no lists/tasks initially

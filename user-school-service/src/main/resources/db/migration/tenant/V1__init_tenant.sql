-- Teachers Table
CREATE TABLE teachers (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    school_id UUID NOT NULL,
    subject_id UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Subjects Table
CREATE TABLE subjects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    school_id UUID NOT NULL
);

-- Lesson Plans Table
CREATE TABLE lesson_plans (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    title VARCHAR(255) NOT NULL,
    grade_level VARCHAR(50) NOT NULL,
    term VARCHAR(50) NOT NULL,
    objectives TEXT,
    document_url VARCHAR(500),
    qdrant_collection_name VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Weekly Schedules Table
CREATE TABLE weekly_schedules (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    subject_id UUID NOT NULL REFERENCES subjects(id),
    lesson_plan_id UUID NOT NULL REFERENCES lesson_plans(id),
    week_number INTEGER NOT NULL,
    day_of_week VARCHAR(50) NOT NULL,
    reminder_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Chat Sessions Table
CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY,
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    started_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    current_context_node VARCHAR(255)
);

-- Messages Table
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    chat_session_id UUID NOT NULL REFERENCES chat_sessions(id),
    direction VARCHAR(20) NOT NULL, -- INCOMING, OUTGOING
    message_type VARCHAR(20) NOT NULL, -- TEXT, VOICE, VIDEO, IMAGE, DOCUMENT
    content_text TEXT,
    media_url VARCHAR(500),
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Media Files Table
CREATE TABLE media_files (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Analytics Records Table
CREATE TABLE analytics_records (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    teacher_id UUID NOT NULL REFERENCES teachers(id),
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Audit Logs Table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    actor VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

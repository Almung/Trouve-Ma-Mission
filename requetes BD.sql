-- Table des utilisateurs
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    position VARCHAR(255),
    role VARCHAR(50) NOT NULL DEFAULT 'USER'
);

-- Table des compétences
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL
);

-- Table des projets
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    client VARCHAR(255) NOT NULL,
    project_manager VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    team_size INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    progress DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Table de liaison projets-compétences
CREATE TABLE project_skills (
    project_id BIGINT REFERENCES projects(id),
    skill_id BIGINT REFERENCES skills(id),
    PRIMARY KEY (project_id, skill_id)
);

-- Table des notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    link VARCHAR(255),
    priority VARCHAR(50) NOT NULL
);

-- Table des collaborateurs
CREATE TABLE collaborators (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(255) NOT NULL,
    grade VARCHAR(255),
    phone VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DISPONIBLE',
    experience_years INTEGER,
    active BOOLEAN NOT NULL DEFAULT true
);

-- Table de liaison collaborateurs-compétences
CREATE TABLE collaborator_skills (
    collaborator_id BIGINT REFERENCES collaborators(id),
    skill_id BIGINT REFERENCES skills(id),
    PRIMARY KEY (collaborator_id, skill_id)
);

-- Table des affectations
CREATE TABLE assignments (
    id BIGSERIAL PRIMARY KEY,
    collaborator_id BIGINT NOT NULL REFERENCES collaborators(id),
    project_id BIGINT NOT NULL REFERENCES projects(id),
    role VARCHAR(255) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL
);




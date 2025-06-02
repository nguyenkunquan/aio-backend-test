CREATE TABLE schedule_job (
                              id VARCHAR(100) PRIMARY KEY,
                              staff_group_id VARCHAR(100) NOT NULL,
                              week_begin_date DATE NOT NULL,
                              status VARCHAR(20) NOT NULL
);

CREATE TABLE shift_assignment (
                                  id SERIAL PRIMARY KEY,
                                  schedule_job_id VARCHAR(100) NOT NULL REFERENCES schedule_job(id) ON DELETE CASCADE,
                                  staff_id VARCHAR(100) NOT NULL,
                                  assignment_date DATE NOT NULL,
                                  shift_type VARCHAR(20) NOT NULL
);

CREATE INDEX idx_schedule_job_id ON shift_assignment(schedule_job_id);
CREATE INDEX idx_schedule_job_status ON schedule_job(status);
CREATE INDEX idx_schedule_job_group_week ON schedule_job(staff_group_id, week_begin_date);

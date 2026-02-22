CREATE TABLE prompt_analysis_results (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    estimated_token_saving INTEGER NOT NULL,
    glacier_melt_reduction_kg DOUBLE PRECISION NOT NULL,
    no_improvement BOOLEAN NOT NULL,
    cannot_improve BOOLEAN NOT NULL,
    xp_earned INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE prompt_analysis_suggestions (
    id UUID PRIMARY KEY,
    result_id UUID NOT NULL REFERENCES prompt_analysis_results(id) ON DELETE CASCADE,
    original_part TEXT NOT NULL,
    improved_part TEXT NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prompt_analysis_results_user_id ON prompt_analysis_results(user_id);
CREATE INDEX idx_prompt_analysis_suggestions_result_id ON prompt_analysis_suggestions(result_id);

package chatbot.service;

import java.util.List;
import java.util.Optional;

import chatbot.entity.Plan;

public interface PlanService {
    List<Plan> getAllPlans();
    Optional<Plan> getPlanById(String id);
    Plan createPlan(Plan plan);
    Plan updatePlan(String id, Plan planDetails);
    void deletePlan(String id);
}


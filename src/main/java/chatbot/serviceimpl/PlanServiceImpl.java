package chatbot.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import chatbot.entity.Plan;
import chatbot.respository.PlanRepository;
import chatbot.service.PlanService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    public Optional<Plan> getPlanById(String id) {
        return planRepository.findById(id);
    }

    @Override
    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }

   

    @Override
    public void deletePlan(String id) {
        planRepository.deleteById(id);
    }

	@Override
	public Plan updatePlan(String id, Plan planDetails) {
		Plan response=planRepository.save(planDetails);
		return response;
		}
}

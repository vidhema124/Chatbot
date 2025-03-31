package chatbot.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import chatbot.entity.Plan;
import chatbot.service.PlanService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

	private final PlanService planService;

	@GetMapping("/getall")
	public ResponseEntity<List<Plan>> getAllPlans() {
		return ResponseEntity.ok(planService.getAllPlans());
	}

	@GetMapping("/byid/{id}")
	public ResponseEntity<Plan> getPlanById(@PathVariable String id) {
		Optional<Plan> plan = planService.getPlanById(id);
		return plan.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/createplan")
	public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
		return ResponseEntity.ok(planService.createPlan(plan));
	}

	@PutMapping("/updatePlanById/{id}")
	public ResponseEntity<Map<String, String>> updatePlanById(@PathVariable("id") String id,
			@RequestBody Plan planDetails) {
		Optional<Plan> existingPlan = planService.getPlanById(id);
		Map<String, String> response = new HashMap<>();

		if (existingPlan.isPresent()) {
			planDetails.setId(id);
			planService.updatePlan(id, planDetails);

			response.put("message", "Plan updated successfully");
			return ResponseEntity.ok(response);
		} else {
			response.put("message", "Plan details do not exist");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

	@DeleteMapping("deletebyid/{id}")
	public ResponseEntity<Map<String, String>> deletePlan(@PathVariable String id) {
		Optional<Plan> existingPlan = planService.getPlanById(id);

		if (existingPlan.isPresent()) {
			planService.deletePlan(id);
			Map<String, String> response = new HashMap<>();
			response.put("message", "Plan deleted successfully");
			return ResponseEntity.ok(response);
		} else {
			Map<String, String> response = new HashMap<>();
			response.put("message", "Plan not found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}
	}

}

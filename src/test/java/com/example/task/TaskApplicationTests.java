package com.example.task;

import com.example.task.entity.Task;
import com.example.task.entity.TaskStatus;
import com.example.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:task-tests;DB_CLOSE_DELAY=-1",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TaskApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void clearTasks() {
		taskRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createTaskReturnsCreatedTaskWithDefaultStatus() throws Exception {
		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"Prepare README\",\"description\":\"Add startup instructions\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.title").value("Prepare README"))
			.andExpect(jsonPath("$.description").value("Add startup instructions"))
			.andExpect(jsonPath("$.status").value("NEW"))
			.andExpect(jsonPath("$.createdAt").isNotEmpty());
	}

	@Test
	void getTasksReturnsListAndTaskById() throws Exception {
		Task task = saveTask("Read documentation", "Review the API", TaskStatus.IN_PROGRESS);

		mockMvc.perform(get("/api/tasks"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(task.getId()))
			.andExpect(jsonPath("$[0].title").value("Read documentation"))
			.andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

		mockMvc.perform(get("/api/tasks/{id}", task.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(task.getId()))
			.andExpect(jsonPath("$.title").value("Read documentation"))
			.andExpect(jsonPath("$.description").value("Review the API"))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.createdAt").isNotEmpty());
	}

	@Test
	void updateTaskStatusChangesStatus() throws Exception {
		Task task = saveTask("Complete task", null, TaskStatus.NEW);

		mockMvc.perform(patch("/api/tasks/{id}/status", task.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"DONE\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(task.getId()))
			.andExpect(jsonPath("$.status").value("DONE"));
	}

	@Test
	void deleteTaskRemovesTask() throws Exception {
		Task task = saveTask("Remove task", null, TaskStatus.NEW);

		mockMvc.perform(delete("/api/tasks/{id}", task.getId()))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tasks/{id}", task.getId()))
			.andExpect(status().isNotFound());
	}

	@Test
	void invalidTaskTitleReturnsBadRequest() throws Exception {
		assertInvalidTaskRequest("", "valid", "title");
		assertInvalidTaskRequest("ab", "valid", "title");
		assertInvalidTaskRequest("x".repeat(101), "valid", "title");
	}

	@Test
	void descriptionLongerThan500CharactersReturnsBadRequest() throws Exception {
		assertInvalidTaskRequest("Valid title", "x".repeat(501), "description");
	}

	@Test
	void unknownTaskStatusReturnsBadRequest() throws Exception {
		mockMvc.perform(patch("/api/tasks/{id}/status", 1)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"UNKNOWN\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation failed"))
			.andExpect(jsonPath("$.fields.status").value("Допустимые значения: NEW, IN_PROGRESS, DONE"));
	}

	@Test
	void malformedRequestBodyReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{invalid json}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fields.request").value("Некорректное тело запроса"));
	}

	@Test
	void missingTaskReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/tasks/999999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404));
	}

	private Task saveTask(String title, String description, TaskStatus taskStatus) {
		Task task = new Task();
		task.setTitle(title);
		task.setDescription(description);
		task.setStatus(taskStatus);
		task.setCreatedAt(java.time.LocalDateTime.now());
		return taskRepository.save(task);
	}

	private void assertInvalidTaskRequest(String title, String description, String invalidField) throws Exception {
		String escapedTitle = title.replace("\\", "\\\\").replace("\"", "\\\"");
		String escapedDescription = description.replace("\\", "\\\\").replace("\"", "\\\"");
		mockMvc.perform(post("/api/tasks")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"" + escapedTitle + "\",\"description\":\""
						+ escapedDescription + "\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("Validation failed"))
			.andExpect(jsonPath("$.fields." + invalidField).exists());
	}
}

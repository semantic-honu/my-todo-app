package com.example.my_todo_app.controller;

import com.example.my_todo_app.config.SecurityConfig;
import com.example.my_todo_app.model.Task;
import com.example.my_todo_app.model.User;
import com.example.my_todo_app.repository.TaskRepository;
import com.example.my_todo_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
public class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test_user");
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(testUser));
    }

    @Test
    @WithMockUser(username = "test_user")
    public void testListTasks() throws Exception {
        when(taskRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())// ステータスコードが200
                .andExpect(view().name("index"))// indexビューが返る
                .andExpect(model().attributeExists("tasks"))// モデルにtasksがある
                .andExpect(model().attributeExists("username"));
    }

    @Test
    @WithMockUser(username = "test_user")
    public void testCreateTask() throws Exception {
        mockMvc.perform(post("/tasks")
                        .with(csrf())
                        .param("title", "New Task")
                        .param("description", "Task Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "test_user")
    public void testShowTaskDetails() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setUser(testUser);

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(model().attribute("task", task));//- モデルに "task" 属性があり、内容が task と一致している

    }

    @Test
    @WithMockUser(username = "test_user")
    public void testUpdateTask() throws Exception {
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setUser(testUser);

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(existingTask));

        mockMvc.perform(post("/tasks/1")
                        .with(csrf())
                        .param("title", "Updated Title")
                        .param("description", "Updated Description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/1"));
    }

    @Test
    @WithMockUser(username = "test_user")
    public void testDeleteTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setUser(testUser);

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(task));

        mockMvc.perform(post("/tasks/1/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testAccessDeniedForUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
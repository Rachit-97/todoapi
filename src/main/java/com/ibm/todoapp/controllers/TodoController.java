package com.ibm.todoapp.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.todoapp.dto.TodoDTO;
import com.ibm.todoapp.exceptions.TodoNotFoundException;
import com.ibm.todoapp.exceptions.UnAuthorizedException;
import com.ibm.todoapp.models.Role;
import com.ibm.todoapp.models.Todo;
import com.ibm.todoapp.models.User;
import com.ibm.todoapp.services.ITodoService;

@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

	private static final Integer Todo = 0;
	@Autowired
	ITodoService todoSvc;

	@GetMapping()
	@PreAuthorize("hasAnyRole('User','Admin')")
	public List<TodoDTO> getAllTodos() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String role = authentication.getAuthorities().iterator().next().getAuthority();

		List<TodoDTO> todoDTO = new ArrayList<>();
		if (role.equals("ROLE_Admin")) {
			List<Todo> todos = todoSvc.getAllTodos();
			todoDTO = todoSvc.TodotoTodoDTO(todos);
		} else if (role.equals("ROLE_User")) {
			String username = authentication.getName();
			List<Todo> todos = todoSvc.getTodoByCreatedBy(username);
			todoDTO = todoSvc.TodotoTodoDTO(todos);
		}
		return todoDTO;

	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('User','Admin')")
	public TodoDTO getByTodoId(@PathVariable Integer id) throws TodoNotFoundException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String role = authentication.getAuthorities().iterator().next().getAuthority();

		TodoDTO todoDTO = new TodoDTO();

		if (role.equals("ROLE_Admin")) {
			Todo todos = todoSvc.getById(id);
			todoDTO = todoSvc.TodotoTodoDTO(todos);
			if (todos == null) {
				throw new TodoNotFoundException("Todo Not Found");
			}
		} else if (role.equals("ROLE_User")) {
			String username = authentication.getName();
			Todo todos = todoSvc.getByIdAndCreatedBy(id, username);
			todoDTO = todoSvc.TodotoTodoDTO(todos);
			if (todos == null) {
				throw new TodoNotFoundException("Todo Not Found");
			}
		}

		return todoDTO;

	}

	@GetMapping("/title/{title}")
	@PreAuthorize("hasAnyRole('User','Admin')")
	public List<TodoDTO> getByTodoTitle(@PathVariable String title) throws TodoNotFoundException{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String role = authentication.getAuthorities().iterator().next().getAuthority();

		List<TodoDTO> todoDTO = null;

		if (role.equals("ROLE_Admin")) {
			List<Todo> todos = todoSvc.getByTitle(title);
			todoDTO = todoSvc.TodotoTodoDTO(todos);
			if (todoDTO.isEmpty()) {
				throw new TodoNotFoundException("Todo Not Found");
			}
		} else if (role.equals("ROLE_User")) {
			String username = authentication.getName();
			List<Todo> todos = todoSvc.getByTitleAndCreatedBy(title, username);
			todoDTO = todoSvc.TodotoTodoDTO(todos);
			if (todos.isEmpty()) {
				throw new TodoNotFoundException("Todo Not Found");
			}
		}

		return todoDTO;

	}

	@PostMapping()
	@PreAuthorize("hasAnyRole('User','Admin')")
	public ResponseEntity<TodoDTO> createTodo(@Valid @RequestBody TodoDTO todoDTO) { // modelbinding ? spring validation
		Todo newTodo = todoSvc.TodoDTOtoTodo(todoDTO);																	// framework
		Todo newTodo1 = todoSvc.addTodo(newTodo);
		TodoDTO todoDTO2 = todoSvc.TodotoTodoDTO(newTodo1);
		return new ResponseEntity<TodoDTO>(todoDTO2, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('User','Admin')")
	public ResponseEntity<TodoDTO> updateTodo(@PathVariable Integer id, @Valid @RequestBody TodoDTO todoDTO) { // modelbinding
																											// ?
		// spring validation
		// framework
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String role = authentication.getAuthorities().iterator().next().getAuthority();

		Todo newTodo = todoSvc.TodoDTOtoTodo(todoDTO);
		
		if (role.equals("ROLE_Admin")) {
			Todo todos = todoSvc.getById(id);
			if (todos != null) {
			Todo todos1 = todoSvc.updateTodo(id, newTodo);
			TodoDTO todoDTO1 = todoSvc.TodotoTodoDTO(todos1);
				return new ResponseEntity<TodoDTO>(todoDTO1, HttpStatus.OK);
			} else {
				throw new UnAuthorizedException("Sorry,your request could not be processed");
			}

		} else if (role.equals("ROLE_User")) {
			String username = authentication.getName();
			Todo todos = todoSvc.getByIdAndCreatedBy(id, username);
			if (todos != null) {
			Todo todos1 = todoSvc.updateTodo(id, newTodo);
			TodoDTO todoDTO1 = todoSvc.TodotoTodoDTO(todos1);
				return new ResponseEntity<TodoDTO>(todoDTO1, HttpStatus.OK);
			} else {
				throw new UnAuthorizedException("Sorry,your request could not be processed");
			}
		} else {
			throw new UnAuthorizedException("Sorry,your request could not be processed");
		}

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('User','Admin')")
	public ResponseEntity<Todo> deleteTodo(@PathVariable Integer id) { // modelbinding ? spring validation framework
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String role = authentication.getAuthorities().iterator().next().getAuthority();

		Todo todos = null;
		if (role.equals("ROLE_Admin")) {
			todos = todoSvc.getById(id);
			if (todos != null) {
				todos = todoSvc.deleteTodo(id);
			} else {
				throw new TodoNotFoundException("Todo Not Found");
			}

		} else if (role.equals("ROLE_User")) {
			String username = authentication.getName();
			todos = todoSvc.getByIdAndCreatedBy(id, username);
			if (todos != null) {
				todoSvc.deleteByIdAndCreatedBy(id, username);
			} else {
				throw new UnAuthorizedException("Sorry,your request could not be processed");
			}
		} else {
			throw new UnAuthorizedException("Sorry,your request could not be processed");
		}
		return null;
	}

	// MethodArgumentNotValidException
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		return errors;
	}
}

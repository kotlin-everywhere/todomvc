package com.minek.kotlin.everywhere.todomvc

import com.minek.kotlin.everywhere.keduct.bluebird.Bluebird
import com.minek.kotlin.everywhere.keduct.uuid.Uuid
import com.minek.kotlin.everywhere.keuix.browser.Cmd
import com.minek.kotlin.everywhere.keuix.browser.Update
import com.minek.kotlin.everywhere.keuix.browser.View
import com.minek.kotlin.everywhere.keuix.browser.html.*
import com.minek.kotlin.everywhere.keuix.browser.html.Html.Companion.section
import com.minek.kotlin.everywhere.keuix.browser.runProgramDebugger
import com.minek.kotlin.everywhere.todomvc.Filter.*
import com.minek.kotlin.everywhere.todomvc.Msg.*
import org.w3c.dom.Element

data class Model(val todos: List<Todo> = listOf(), val newTodoMessage: String = "", val editing: Editing? = null, val filter: Filter = All)

fun Model.updateTodo(id: Uuid, update: (Todo) -> Todo): Model {
    return copy(todos = todos.map { if (it.id == id) update(it) else it })
}

val Model.activeTodos: List<Todo>
    get() = todos.filter { !it.completed }

val Model.completedTodos: List<Todo>
    get() = todos.filter { it.completed }

val Model.filteredTodos: List<Todo>
    get() = when (filter) {
        All -> todos
        Completed -> completedTodos
        Active -> activeTodos
    }

sealed class Filter {
    object All : Filter()
    object Completed : Filter()
    object Active : Filter()
}

data class Todo(val id: Uuid, val message: String = "", val completed: Boolean = false)

val Todo.editElementId: String
    get() = "todo-edit-$id"

data class Editing(val id: Uuid, val message: String = "")

sealed class Msg {
    data class SetNewTodoMessage(val message: String) : Msg()
    data class SetFilter(val filter: Filter) : Msg()
    data class SetCompleted(val completed: Boolean) : Msg()
    object ClearCompleted : Msg()

    object AddTodo : Msg()
    data class NewUuid(val uuid: Uuid) : Msg()

    data class SetTodoCompleted(val id: Uuid, val completed: Boolean) : Msg()
    data class StartEditTodo(val id: Uuid) : Msg()
    data class SetEditTodoMessage(val id: Uuid, val message: String) : Msg()
    data class CommitEditTodo(val id: Uuid) : Msg()
    data class CancelEditTodo(val id: Uuid) : Msg()
    data class DeleteTodo(val id: Uuid) : Msg()
}

val update: Update<Model, Msg> = { msg, model ->
    when (msg) {
        is SetNewTodoMessage -> model.copy(newTodoMessage = msg.message) to null
        is SetFilter -> model.copy(filter = msg.filter) to null
        is SetCompleted -> model.copy(todos = model.todos.map { it.copy(completed = msg.completed) }) to null
        ClearCompleted -> model.copy(todos = model.activeTodos) to null

        AddTodo -> model to if (model.newTodoMessage.isNotEmpty()) generateUuid(::NewUuid) else null
        is NewUuid -> model.copy(newTodoMessage = "", todos = model.todos + Todo(msg.uuid, model.newTodoMessage)) to null

        is SetTodoCompleted -> model.updateTodo(msg.id) { it.copy(completed = msg.completed) } to null
        is StartEditTodo -> {
            model.todos.firstOrNull { it.id == msg.id }?.let {
                model.copy(editing = Editing(it.id, it.message)) to Cmd.focus<Msg>(it.editElementId)
            } ?: model to null
        }
        is SetEditTodoMessage -> {
            (if (model.editing?.id == msg.id) {
                model.copy(editing = model.editing.copy(message = msg.message))
            } else model) to null
        }
        is CancelEditTodo -> {
            (if (model.editing?.id == msg.id) model.copy(editing = null) else model) to null
        }
        is CommitEditTodo -> {
            (if (model.editing != null && model.editing.id == msg.id) {
                val message = model.editing.message.trim()
                val newModel = (if (message.isNotEmpty())
                    model.updateTodo(msg.id) { it.copy(message = model.editing.message) }
                else
                    model.copy(todos = model.todos.filter { it.id != msg.id }))
                newModel.copy(editing = null)
            } else model) to null
        }
        is DeleteTodo -> model.copy(todos = model.todos.filter { it.id != msg.id }) to null
    }
}

val view: View<Model, Msg> = { model ->
    section(class_("todoapp")) {
        header(class_("header")) {
            h1(text = "todos")
            input(class_("new-todo"), placeholder("What needs to be done?"), autofocus(true), value(model.newTodoMessage), onInput(::SetNewTodoMessage), onEnter(AddTodo))
        }

        if (model.todos.isNotEmpty()) {
            section(class_("main")) {
                input(id("toggle-all"), class_("toggle-all"), type("checkbox"), checked(model.activeTodos.isEmpty()), onCheck(::SetCompleted))
                label(for_("toggle-all"), text = "Mark all as complete")
                ul(class_("todo-list")) {
                    model.filteredTodos.forEach { todo ->
                        li(key("todo-${todo.id}"), classes("completed" to todo.completed, "editing" to (model.editing?.id == todo.id))) {
                            div(class_("view")) {
                                input(class_("toggle"), type("checkbox"), checked(todo.completed), onCheck { SetTodoCompleted(todo.id, it) })
                                label(onDblclick(StartEditTodo(todo.id)), text = todo.message)
                                button(class_("destroy"), onClick(DeleteTodo(todo.id)))
                            }
                            if (model.editing?.id == todo.id) {
                                input(id(todo.editElementId), class_("edit"), autofocus(true), value(todo.message),
                                        onInput { SetEditTodoMessage(todo.id, it) },
                                        onEscape(CancelEditTodo(todo.id)),
                                        onBlur(CommitEditTodo(todo.id)), onEnter(CommitEditTodo(todo.id)))
                            }
                        }
                    }
                }
            }

            footer(class_("footer")) {
                span(class_("todo-count")) {
                    strong(text = "${model.activeTodos.size}")
                    +" item left"
                }
                ul(class_("filters")) {
                    arrayOf(All to "All", Active to "Active", Completed to "Completed").forEach { (filter, label) ->
                        li {
                            a(classes("selected" to (filter === model.filter)), href("#/"), onClick(SetFilter(filter), true), text = label)
                        }
                    }
                }
                if (model.completedTodos.isNotEmpty()) {
                    button(class_("clear-completed"), onClick(ClearCompleted), text = "Clear completed")
                }
            }
        }
    }
}

@Suppress("unused")
@JsName("main")
fun main(container: Element) {
    runProgramDebugger(container, Model(), update, view)
}

fun <T : Any> generateUuid(tagger: (Uuid) -> T): Cmd<T> {
    return Cmd.wrap { Bluebird.resolve(tagger(Uuid.randomUuid())) }
}
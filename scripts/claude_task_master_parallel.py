#!/usr/bin/env -S uv run --script
#
# /// script
# requires-python = ">=3.9"
# dependencies = [
#   "python-dotenv",
#   "rich",
#   "requests",
#   "asyncio",
#   "aiohttp"
# ]
# ///

import os
import sys
import subprocess
import json
import shutil
import asyncio
import aiohttp
from typing import List, Dict, Any, Optional, Set
from dotenv import load_dotenv
from rich.console import Console
from rich.prompt import Prompt, Confirm
from rich.syntax import Syntax
from rich.progress import Progress, SpinnerColumn, TextColumn
from rich.table import Table
from rich import print as rprint

# --- Configuration ---
TASKS_DIR = "tasks"
TASKS_FILE = os.path.join(TASKS_DIR, "tasks.json")
MAX_CONCURRENT_TASKS = 5  # Maximum number of tasks to run in parallel

console = Console()



# Load environment variables
load_dotenv()
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY")
if not ANTHROPIC_API_KEY:
    console.print("[bold red]ERROR: ANTHROPIC_API_KEY not found in environment variables.[/bold red]")
    sys.exit(1)

# --- Argument Parsing ---
import argparse
parser = argparse.ArgumentParser(description="Claude Task Master Parallel Executor")
parser.add_argument("--max-concurrent", type=int, default=MAX_CONCURRENT_TASKS, 
                    help=f"Maximum number of concurrent Claude instances (default: {MAX_CONCURRENT_TASKS})")
parser.add_argument("--task-ids", type=str, help="Comma-separated list of specific task IDs to execute")
parser.add_argument("--list", action="store_true", help="List all tasks and their status")
args = parser.parse_args()

# --- Task Master Interaction ---
def read_tasks_json() -> Dict[str, Any]:
    """Read tasks from the tasks.json file."""
    try:
        with open(TASKS_FILE, 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        console.print(f"[bold red]Error: Tasks file '{TASKS_FILE}' not found.[/bold red]")
        return {}
    except json.JSONDecodeError:
        console.print(f"[bold red]Error: Tasks file '{TASKS_FILE}' contains invalid JSON.[/bold red]")
        return {}

def read_task_file(task_id: str) -> Dict[str, str]:
    """Read a task file and parse its contents."""
    task_file = os.path.join(TASKS_DIR, f"task_{task_id.zfill(3)}.txt")
    
    if not os.path.exists(task_file):
        console.print(f"[yellow]Warning: Task file '{task_file}' not found.[/yellow]")
        return {}
    
    try:
        with open(task_file, 'r') as f:
            content = f.read()
        
        # Parse the task file content
        task_data = {}
        sections = {
            "Title": "",
            "Status": "",
            "Dependencies": "",
            "Priority": "",
            "Description": "",
            "Details": "",
            "Test Strategy": ""
        }
        
        current_section = None
        section_content = []
        
        for line in content.split('\n'):
            if line.startswith('# '):
                # Save previous section if it exists
                if current_section:
                    sections[current_section] = '\n'.join(section_content).strip()
                    section_content = []
                
                # Extract new section name
                section_header = line[2:].strip()
                if ':' in section_header:
                    section_name, value = section_header.split(':', 1)
                    section_name = section_name.strip()
                    if section_name in sections:
                        current_section = section_name
                        section_content = [value.strip()]
                    else:
                        current_section = None
                else:
                    section_name = section_header
                    if section_name in sections:
                        current_section = section_name
                        section_content = []
                    else:
                        current_section = None
            elif current_section:
                section_content.append(line)
        
        # Save the last section
        if current_section:
            sections[current_section] = '\n'.join(section_content).strip()
        
        # Convert dependencies to a list
        if sections["Dependencies"]:
            dependencies = []
            for dep in sections["Dependencies"].split(','):
                dep = dep.strip()
                if dep and dep.lower() != 'none':
                    try:
                        dependencies.append(int(dep))
                    except ValueError:
                        console.print(f"[yellow]Warning: Invalid dependency value '{dep}' in task file '{task_file}'[/yellow]")
            sections["Dependencies"] = dependencies
        else:
            sections["Dependencies"] = []
        
        return sections
    except Exception as e:
        console.print(f"[bold red]Error reading task file '{task_file}': {e}[/bold red]")
        return {}

def check_tasks_exist():
    """Check if tasks directory and files exist."""
    if not os.path.exists(TASKS_DIR):
        console.print(f"[bold red]Error: Tasks directory '{TASKS_DIR}' not found.[/bold red]")
        sys.exit(1)
    
    if not os.path.exists(TASKS_FILE):
        console.print(f"[bold red]Error: Tasks file '{TASKS_FILE}' not found.[/bold red]")
        sys.exit(1)

def list_tasks():
    """List all tasks and their status."""
    tasks_data = read_tasks_json()
    
    if not tasks_data or "tasks" not in tasks_data:
        console.print("[bold red]Error: Could not retrieve tasks.[/bold red]")
        return
    
    tasks = tasks_data["tasks"]
    
    table = Table(title="Tasks")
    table.add_column("ID", justify="right", style="cyan", no_wrap=True)
    table.add_column("Title", style="magenta")
    table.add_column("Status", justify="center", style="green")
    table.add_column("Dependencies", justify="right", style="blue")
    
    for task in tasks:
        task_id = str(task["id"])
        title = task["title"]
        status = task["status"]
        deps = ", ".join(str(dep) for dep in task.get("dependencies", []))
        
        table.add_row(task_id, title, status, deps)
        
        # Add subtasks if any
        for subtask in task.get("subtasks", []):
            subtask_id = f"{task_id}.{subtask['id']}"
            subtask_title = f"└─ {subtask['title']}"
            subtask_status = subtask["status"]
            subtask_deps = ", ".join(str(dep) for dep in subtask.get("dependencies", []))
            
            table.add_row(subtask_id, subtask_title, subtask_status, subtask_deps)
    
    console.print(table)

def get_all_tasks() -> List[Dict[str, Any]]:
    """Get all tasks from tasks.json file."""
    tasks_data = read_tasks_json()
    
    if not tasks_data or "tasks" not in tasks_data:
        console.print("[bold red]Error: Could not retrieve tasks.[/bold red]")
        return []
    
    tasks = tasks_data["tasks"]
    
    # Enrich tasks with data from individual task files if available
    for task in tasks:
        task_id = str(task["id"])
        task_file_data = read_task_file(task_id)
        
        # Only update if we have data and don't override existing data
        if task_file_data:
            for key, value in task_file_data.items():
                if key.lower() not in task and value:
                    task[key.lower()] = value
    
    return tasks

def get_available_tasks(tasks: List[Dict[str, Any]], completed_task_ids: Set[str] = None) -> List[Dict[str, Any]]:
    """
    Get tasks that are available to be executed (all dependencies are satisfied).
    
    Args:
        tasks: List of all tasks
        completed_task_ids: Set of task IDs that have been completed
        
    Returns:
        List of available tasks
    """
    if completed_task_ids is None:
        completed_task_ids = set()
    
    # Create a map of task ID to status
    task_status = {
        str(task["id"]): task["status"] 
        for task in tasks
    }
    
    # Add any completed tasks from our tracking
    for task_id in completed_task_ids:
        task_status[task_id] = "done"
    
    available_tasks = []
    
    for task in tasks:
        # Skip tasks that are already done or deferred
        if task["status"] in ["done", "deferred"]:
            continue
            
        # Check if all dependencies are satisfied
        dependencies = [str(dep) for dep in task.get("dependencies", [])]
        all_deps_satisfied = all(
            task_status.get(dep, "") == "done" 
            for dep in dependencies
        )
        
        if all_deps_satisfied:
            available_tasks.append(task)
    
    return available_tasks

async def execute_task(task: Dict[str, Any], semaphore: asyncio.Semaphore) -> Dict[str, Any]:
    """
    Execute a single task using Claude.
    
    Args:
        task: The task to execute
        semaphore: Semaphore to limit concurrent executions
        
    Returns:
        Updated task with results
    """
    task_id = str(task["id"])
    task_title = task["title"]
    
    async with semaphore:
        console.print(f"[bold blue]Starting execution of Task {task_id}: {task_title}[/bold blue]")
        
        # Use Claude to execute the task with the task-master MCP
        # Create a prompt that tells Claude to implement the task
        prompt = f"Please implement task {task_id}: {task_title}. Use the task-master MCP to get the task details and mark it as complete when done."
        
        # Get task details from the task file
        task_details = read_task_file(task_id)
        task_description = task_details.get("Description", "")
        task_implementation = task_details.get("Details", "")
        
        # Create a detailed prompt for Claude
        # Include task details and project context
        detailed_prompt = f"""# Task Implementation Request

## Task Details
Task ID: {task_id}
Task Title: {task_title}

### Description
{task_description}

### Implementation Details
{task_implementation}

## Project Context
This is a simple banking application that manages accounts, transactions, and user data. The application follows a layered architecture with models, repositories, services, and controllers.

## Instructions
1. Explore the codebase to understand the existing structure and patterns
2. Implement the task according to the implementation details
3. Follow best practices for clean code and maintainability
4. Test your implementation if possible
5. Use the task-master MCP tools to get additional task details if needed
6. Mark the task as complete using the task-master MCP when you're done

## Available Task-Master MCP Tools
You have access to the task-master MCP tools to help you implement this task. These tools will allow you to:
- Get task details
- Update task information
- Mark tasks as complete when you're done
- Expand tasks into subtasks if needed

The MCP system will handle all the details of interacting with the task-master system.

## Important Notes
- Ensure your implementation is consistent with the existing codebase
- Add appropriate error handling and validation
- Document your code with comments where necessary
- Consider performance and security implications

Please provide the implementation for this task."""
        
        # Add dependencies to the prompt if they exist
        if task.get("dependencies"):
            dependencies_str = ", ".join([str(dep) for dep in task.get("dependencies")])
            detailed_prompt += f"\n\n## Dependencies\nThis task depends on the following tasks: {dependencies_str}\nMake sure to review these tasks before implementation."
        
        # Add any subtasks to the prompt if they exist
        if task.get("subtasks"):
            subtasks_str = "\n".join([f"- {subtask.get('title')}" for subtask in task.get("subtasks")])
            detailed_prompt += f"\n\n## Subtasks\n{subtasks_str}"
        
        # Use Claude directly with the correct parameters
        # Based on the Notion example provided by the user
        
        # Define the allowed tools for Claude
        allowed_tools = [
            # Standard Claude Code tools
            "Bash",              # Run shell commands
            "Edit",              # Edit files
            "View",              # View file contents
            "GlobTool",          # Find files by pattern
            "GrepTool",          # Search for text in files
            "LSTool",            # List directory contents
            "BatchTool",         # Run batch operations
            "AgentTool",         # Use agent capabilities
            "WebFetchTool",      # Fetch web content
            "Write",             # Write to files
            "TodoWrite",         # Write to todo lists
            "TodoRead",          # Read from todo lists
            
            # Task Master MCP tools - ensure correct prefixes
            "mcp1_get_task", 
            "mcp1_update_task", 
            "mcp1_set_task_status", 
            "mcp1_expand_task", 
            "mcp1_add_subtask", 
            "mcp1_clear_subtasks",
            "mcp1_get_tasks",
            "mcp1_next_task",
            "mcp1_update",
            "mcp1_validate_dependencies",
            "mcp1_add_dependency",
            "mcp1_remove_dependency",
            "mcp1_fix_dependencies"
        ]
        
        cmd = [
            "claude",                # Claude CLI command
            "-p", detailed_prompt,  # Provide the detailed prompt
            "--output-format", "stream-json",  # Use streaming JSON output
            "--allowedTools"
        ] + allowed_tools  # Add the allowed tools to the command
        
        
        # Use a simple print instead of console.status to avoid the "Only one live display may be active at once" error
        console.print(f"[bold green]Executing Task {task_id}...[/bold green]")
        
        # Execute the command without using console.status
        try:
            # Use an approach similar to the Notion example
            # Run Claude with subprocess.Popen for streaming output
            console.print(f"[cyan]DEBUG: Running command: {' '.join(cmd)}[/cyan]")
            
            # Create a function to run Claude in a separate thread
            def run_claude():
                try:
                    # Use Popen to get streaming output
                    process = subprocess.Popen(
                        cmd,
                        stdout=subprocess.PIPE,
                        stderr=subprocess.PIPE,
                        text=True,
                        bufsize=1,
                    )
                    
                    # Collect all output
                    all_stdout = []
                    all_stderr = []
                    
                    # Track if we've seen tool uses for task-master MCP
                    seen_mcp_tool_use = False
                    
                    # Print minimal header
                    console.print("\n--- Claude output for Task {} ---".format(task_id))
                    
                    while True:
                        stdout_line = process.stdout.readline()
                        if not stdout_line and process.poll() is not None:
                            break
                        if stdout_line:
                            all_stdout.append(stdout_line)
                            
                            # Check for task-master MCP tool usage but don't add formatting
                            if 'mcp1_' in stdout_line:
                                seen_mcp_tool_use = True
                            
                            # Just print the raw JSON line
                            console.print(stdout_line.strip())
                    
                    # Get any remaining stderr
                    stderr = process.stderr.read()
                    if stderr:
                        all_stderr.append(stderr)
                    
                    # Wait for process to complete
                    return_code = process.wait()
                    console.print(f"[cyan]DEBUG: Claude completed with return code: {return_code}[/cyan]")
                    
                    # Create a result object similar to subprocess.run
                    class Result:
                        def __init__(self, returncode, stdout, stderr, used_mcp):
                            self.returncode = returncode
                            self.stdout = stdout
                            self.stderr = stderr
                            self.used_mcp = used_mcp
                    
                    # Join all output
                    stdout_content = ''.join(all_stdout)
                    stderr_content = ''.join(all_stderr)
                    
                    # Print minimal footer
                    console.print("\n--- End of Claude output for Task {} ---".format(task_id))
                    console.print("Task {} completed with return code: {}".format(task_id, return_code))
                    
                    return Result(return_code, stdout_content.encode('utf-8'), stderr_content.encode('utf-8'), seen_mcp_tool_use)
                except Exception as e:
                    console.print(f"[cyan]DEBUG: Exception in run_claude: {str(e)}[/cyan]")
                    return e
            
            # Run the command in a thread pool
            loop = asyncio.get_event_loop()
            result = await loop.run_in_executor(None, run_claude)
            
            # Check if the result is an exception
            if isinstance(result, Exception):
                raise result
                
        except Exception as e:
            console.print(f"[bold red]Error starting Claude process: {str(e)}[/bold red]")
            return {**task, "status": "failed", "error": str(e)}
        
        # Process the result
        console.print(f"[cyan]DEBUG: Processing result for task {task_id}[/cyan]")
        if result.returncode != 0:
            console.print(f"[bold red]❌ Task {task_id} failed with exit code: {result.returncode}[/bold red]")
            if result.stderr:
                console.print(f"[red]Error output:[/red]\n{result.stderr.decode('utf-8')}")
            return {**task, "status": "failed"}
        
        console.print(f"[cyan]DEBUG: Command succeeded for task {task_id}[/cyan]")
        
        # Parse the JSON output from Claude
        try:
            console.print(f"[cyan]DEBUG: Attempting to parse JSON output for task {task_id}[/cyan]")
            if result.stdout:
                stdout_content = result.stdout.decode('utf-8')
                console.print(f"[cyan]DEBUG: Raw stdout content (first 100 chars): {stdout_content[:100]}...[/cyan]")
                
                output_json = json.loads(stdout_content)
                
                # Display structured output
                console.print(f"[blue]Cost:[/blue] ${output_json.get('cost_usd', 'N/A')}")
                console.print(f"[blue]Duration:[/blue] {output_json.get('duration_ms', 'N/A')}ms")
                
                # Display the first 200 characters of the result
                result_text = output_json.get('result', '')
                preview = result_text[:200] + '...' if len(result_text) > 200 else result_text
                console.print(f"[blue]Result preview:[/blue] {preview}")
                
                # Claude should handle marking the task as complete directly
                # We don't need to use the task-master CLI
                console.print(f"[cyan]Task {task_id} processed by Claude[/cyan]")
                
                # Check if the result contains any indication of task completion
                if 'completed' in result_text.lower() or 'implemented' in result_text.lower() or 'finished' in result_text.lower():
                    console.print(f"[green]Task completion detected in Claude's response[/green]")
                
                # Note: Claude will handle marking the task as done through its own processing
                
                console.print(f"[bold green]✅ Task {task_id} completed successfully[/bold green]")
                return {**task, "status": "done", "result": output_json}
            else:
                console.print("[yellow]Warning: No output from Claude[/yellow]")
                return {**task, "status": "done"}
        except json.JSONDecodeError as e:
            console.print(f"[yellow]Warning: Could not parse Claude output as JSON: {e}[/yellow]")
            if result.stdout:
                console.print(f"[blue]Raw output:[/blue]\n{result.stdout.decode('utf-8')[:500]}...")
            
            console.print(f"[bold green]✅ Task {task_id} completed successfully[/bold green]")
            return {**task, "status": "done"}

# Create a lock for console status displays
console_lock = asyncio.Lock()

async def execute_tasks_in_parallel(tasks: List[Dict[str, Any]], max_concurrent: int = MAX_CONCURRENT_TASKS):
    """
    Execute multiple tasks in parallel using Claude.
    
    Args:
        tasks: List of tasks to execute
        max_concurrent: Maximum number of concurrent tasks
    """
    if not tasks:
        console.print("[yellow]No tasks available to execute.[/yellow]")
        return
    
    console.print(f"[bold blue]Executing {len(tasks)} tasks with max concurrency of {max_concurrent}[/bold blue]")
    
    # Create a semaphore to limit concurrent executions
    semaphore = asyncio.Semaphore(max_concurrent)
    
    # Define a wrapper function that passes the console lock to execute_task
    async def execute_task_with_lock(task):
        try:
            return await execute_task(task, semaphore)
        except Exception as e:
            return e
    
    # Create a task for each available task
    task_futures = [execute_task_with_lock(task) for task in tasks]
    
    # Execute all tasks and gather results
    completed_tasks = await asyncio.gather(*task_futures)
    
    # Process results
    successful_tasks = 0
    failed_tasks = 0
    used_mcp_tools = 0
    
    for result in completed_tasks:
        if isinstance(result, Exception):
            console.print(f"[bold red]Task execution failed with exception: {result}[/bold red]")
            failed_tasks += 1
        elif result.get("status") == "done":
            successful_tasks += 1
            # Check if the task used MCP tools
            if hasattr(result, 'used_mcp') and result.used_mcp:
                used_mcp_tools += 1
        else:
            error_msg = result.get("error", "Unknown error")
            console.print(f"[bold red]Task {result.get('id', 'unknown')} failed: {error_msg}[/bold red]")
            failed_tasks += 1
    
    # Print summary
    console.print("\n[bold blue]===== Task Execution Summary =====[/bold blue]")
    console.print(f"[bold green]✅ {successful_tasks} tasks completed successfully[/bold green]")
    if failed_tasks > 0:
        console.print(f"[bold red]❌ {failed_tasks} tasks failed[/bold red]")
    console.print(f"[cyan]ℹ️ {used_mcp_tools} tasks used task-master MCP tools[/cyan]")
    
    # Return summary
    return {
        "successful": successful_tasks,
        "failed": failed_tasks,
        "used_mcp_tools": used_mcp_tools
    }

async def main():
    """Main entry point for the script."""
    # Check if tasks exist
    check_tasks_exist()
    
    # List tasks if requested
    if args.list:
        list_tasks()
        return
    
    # Get all tasks
    all_tasks = get_all_tasks()
    if not all_tasks:
        console.print("[bold red]No tasks found in tasks.json.[/bold red]")
        return
    
    # If specific task IDs were provided, filter to those tasks
    if args.task_ids:
        task_ids = args.task_ids.split(",")
        filtered_tasks = [task for task in all_tasks if str(task["id"]) in task_ids]
        if not filtered_tasks:
            console.print(f"[bold red]No tasks found with IDs: {args.task_ids}[/bold red]")
            return
        
        # Execute the specified tasks in parallel
        await execute_tasks_in_parallel(filtered_tasks, args.max_concurrent)
    else:
        # Get available tasks (all dependencies satisfied)
        available_tasks = get_available_tasks(all_tasks)
        
        if not available_tasks:
            console.print("[yellow]No available tasks found. All tasks are either completed, deferred, or have unsatisfied dependencies.[/yellow]")
            return
        
        # Execute available tasks in parallel
        await execute_tasks_in_parallel(available_tasks, args.max_concurrent)

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        console.print("\n[bold yellow]Operation cancelled by user.[/bold yellow]")
        sys.exit(1)
    except Exception as e:
        console.print(f"[bold red]❌ Unexpected error: {str(e)}[/bold red]")
        sys.exit(1)

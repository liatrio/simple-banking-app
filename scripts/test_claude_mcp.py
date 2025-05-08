#!/usr/bin/env python3

import subprocess
import os
import json
from rich.console import Console

console = Console()

def main():
    console.print("[bold blue]Testing Claude with MCP config[/bold blue]")
    
    # Try different Claude CLI parameters
    
    # Simple prompt for testing
    prompt = "Say hello world and tell me what MCP servers you have access to."
    
    # Test 1: Basic Claude command with --test false parameter
    cmd1 = [
        "claude",                # Claude CLI command
        "-p", prompt,           # Provide the prompt
        "--test", "false",      # Try the --test false parameter
        "--output-format", "json"  # Get structured JSON output
    ]
    
    # Test 2: Claude with MCP config file
    mcp_config_path = os.path.expanduser("~/.codeium/windsurf/mcp_config.json")
    cmd2 = [
        "claude",                # Claude CLI command
        "-p", prompt,           # Provide the prompt
        "--mcp-config", mcp_config_path,  # Use the MCP config file
        "--output-format", "json"  # Get structured JSON output
    ]
    
    # Test 3: Claude with MCP server directly
    cmd3 = [
        "claude",                # Claude CLI command
        "mcp",                  # MCP subcommand
        "list"                  # List MCP servers
    ]
    
    # Test 4: Claude with the registered taskmaster-ai MCP server
    cmd4 = [
        "claude",                # Claude CLI command
        "-p", "Please implement task 18: Implement Dynamic Credit Limits. Use the task-master MCP to get the task details and mark it as complete when done.",  # Task-specific prompt
        "--mcp-server", "taskmaster-ai"  # Use the registered MCP server
    ]
    
    # Choose which test to run
    cmd = cmd4  # Use the registered taskmaster-ai MCP server
    
    console.print(f"[cyan]Running command: {' '.join(cmd)}[/cyan]")
    
    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            check=False
        )
        
        console.print(f"[cyan]Command completed with return code: {result.returncode}[/cyan]")
        
        if result.returncode != 0:
            console.print(f"[bold red]Command failed with exit code: {result.returncode}[/bold red]")
            if result.stderr:
                console.print(f"[red]Error output:[/red]\n{result.stderr.decode('utf-8')}")
            return
        
        console.print("[cyan]Command succeeded[/cyan]")
        
        # Parse the JSON output from Claude
        try:
            if result.stdout:
                stdout_content = result.stdout.decode('utf-8')
                console.print(f"[cyan]Raw stdout content (first 100 chars): {stdout_content[:100]}...[/cyan]")
                
                output_json = json.loads(stdout_content)
                
                # Display structured output
                console.print(f"[blue]Cost:[/blue] ${output_json.get('cost_usd', 'N/A')}")
                console.print(f"[blue]Duration:[/blue] {output_json.get('duration_ms', 'N/A')}ms")
                
                # Display the result
                result_text = output_json.get('result', '')
                console.print(f"[green]Result:[/green]\n{result_text}")
            else:
                console.print("[yellow]Warning: No output from Claude[/yellow]")
        except json.JSONDecodeError as e:
            console.print(f"[yellow]Warning: Could not parse Claude output as JSON: {e}[/yellow]")
            if result.stdout:
                console.print(f"[blue]Raw output:[/blue]\n{result.stdout.decode('utf-8')}")
    
    except Exception as e:
        console.print(f"[bold red]Error running Claude: {str(e)}[/bold red]")

if __name__ == "__main__":
    main()

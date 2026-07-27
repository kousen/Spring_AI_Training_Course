#!/bin/bash

# Script to verify solutions branch has complete implementations
echo "🔍 Verifying solutions branch integrity..."

# Check if we're on solutions branch
current_branch=$(git branch --show-current)
if [ "$current_branch" != "solutions" ]; then
    echo "❌ Must be on solutions branch. Current: $current_branch"
    exit 1
fi

# Count TODO comments in test files (should be 0 on solutions branch)
todo_count=$(find src/test -name "*.java" -exec grep -l "// TODO:" {} \; 2>/dev/null | wc -l)

if [ "$todo_count" -gt 0 ]; then
    echo "❌ Found TODO comments in test files on solutions branch!"
    echo "Files with TODOs:"
    find src/test -name "*.java" -exec grep -l "// TODO:" {} \; 2>/dev/null
    echo ""
    echo "Solutions branch should have complete implementations, not TODO stubs."
    echo "Use: git checkout <good-commit> -- src/test/java/com/oreilly/springaicourse/"
    exit 1
fi

# Check for specific implementation methods that should exist
echo "✅ No TODO comments found in test files"

# Verify key implementation files exist and have content
key_files=(
    "src/test/java/com/oreilly/springaicourse/OpenAiTests.java"
    "src/test/java/com/oreilly/springaicourse/ClaudeTests.java"
    "src/test/java/com/oreilly/springaicourse/RAGTests.java"
    "src/test/java/com/oreilly/springaicourse/AudioTests.java"
    "src/main/java/com/oreilly/springaicourse/RAGService.java"
    "src/main/java/com/oreilly/springaicourse/AppConfig.java"
)

for file in "${key_files[@]}"; do
    if [ ! -f "$file" ]; then
        echo "❌ Missing file: $file"
        exit 1
    fi
    
    # Check file has substantial content (more than just imports and class declaration)
    line_count=$(wc -l < "$file")
    if [ "$line_count" -lt 50 ]; then
        echo "❌ File too small (likely stub): $file ($line_count lines)"
        exit 1
    fi
done

echo "✅ All key implementation files present and substantial"

# Check for specific method implementations
if ! grep -q "chatClient.prompt()" src/test/java/com/oreilly/springaicourse/OpenAiTests.java; then
    echo "❌ OpenAiTests.java missing working implementations"
    exit 1
fi

if ! grep -q "questionAnswerAdvisor" src/main/java/com/oreilly/springaicourse/RAGService.java; then
    echo "❌ RAGService.java missing complete RAG implementation"
    exit 1
fi

echo "✅ Key implementations verified"
echo "🎉 Solutions branch integrity check PASSED!"
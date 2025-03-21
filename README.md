# [In-Progress]
# Idea 4: "Quiz Coach: AI-Powered Personal Tutor"
**Synopsis:** Develop a personal coach application that quizzes students to check their understanding of the learning material. The AI will generate questions based on the provided study material and track the student's progress. This tool aims to reinforce learning by providing regular quizzes and feedback, helping students identify areas where they need improvement.

**Key Features:**
1. AI-generated quizzes based on study material
2. Progress tracking and performance analysis
3. Personalized feedback and recommendations
4. Customizable quiz settings (e.g., difficulty, topics)
5. User-friendly interface for easy interaction

## \<Exceptional Handlers/> Team Members
- Joshua
- Jack
- Justin
- Mason
- Kiel

## Git Command Information and Instructions
### Project Connection
- Create new IntelliJ Project
- Connect to this Git Repo (git remote add origin https://github.com/TheTrueJM/CAB302-Exceptional-Handlers.git)
- Download all Files (git pull origin main)

### Adding Names to this File
- Make a new Branch with your Name (git branch "ActualName")
- Go to your Branch (git checkout "ActualName") 
- Add your Name to this File above
- Stage you change (git add .)
- Commit your change (git commit -m "Added my Name")
- Push your change (git push --set-upstream origin "ActualName")
- Go to the Main Branch (git checkout main)
- Merge your Branch to the Main Branch (git merge "Name")
- Push the Merge (git push)

### Making your own changes to this repo
Before making any changes make sure you're in the right branch
```bash
$ git branch
* MyNameBranch 
main
```
Wherever the `*` is at, thats the current selected branch.

If it isn't at the branch you want, you do this
```bash
$ git switch <branch name>
```

Now, before you make any changes MAKE SURE YOUR BRANCH IS UP TO DATE TO THE MAIN BRANCH
```
$ git fetch origin
$ git pull origin main
```

Next, do the changes you need, stage them, then commit:
```bash
# Do your changes here...

# Add the relevant files into staging
$ git add <file 1> <file 2> <file 3> <file n>
$ git add . # If you want to stage ALL changes

# If you want to unstage files
$ git restore --staged <file>

# Commit those changes
$ git status # IF you want to see what's staged and unstaged, untracked files
$ git commit -m "my new commit"

# Now push to your branch
$ git push origin <your-branch-name>
```

Finally, on github, make a Pull Request (**PR**) using your branch: https://github.com/TheTrueJM/CAB302-Exceptional-Handlers/pulls

**Notes:**
- You can keep working making edits after you open a PR, just keep committing and pushing your code. No need to make a new branch or anything like that
- Make sure the team gives the green flag on your changes before we merge them into then main branch
- If you stage a file then make changes, you have to stage them again with `git add`

### Handling merge conflicts
Ping us on discord

### Reverting commits (If you haven't pushed yet)
You can revert your commit by doing this
```bash
# If you commit accidentally, or forgot to add something
$ git commit -m "my bad & broken commit"

# Simply do this
$ git reset --soft HEAD~1

# Now, modify the changes you wanted to do originally, and then commit
$ git commit -m "my happy code"
$ git push origin <your branch>
```

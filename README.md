# [In-Progress]
# Idea 4: "Quiz Coach: AI-Powered Personal Tutor"

[Instructions for Project Connection]
- Create new IntelliJ Project
- Connect to this Git Repo (git remote add origin https://github.com/TheTrueJM/CAB302-Exceptional-Handlers.git)
- Download all Files (git pull origin main)

## \<Exceptional Handlers/> Team Members
- Joshua
- Jack
- Justin
- Mason
- Kiel (n11409851@qut.edu.au)

### Instructions for Adding Names
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
* Justin 
main
```
so like wherever the `*` is at, thats the current selected branch.

If it isn't at the branch you want, you do this
```bash
git switch <branch name>
```

Now before you make any changes MAKE SURE YOUR BRANCH IS UP TO DATE TO THE MAIN BRANCH
```
git fetch origin
git pull origin main
```

Next do the changes you need, stage them, commit:
```bash
# do your changes here blah blah blah

# then add the relevant files into staging
git add <file 1> <file 2> <file 3> <file n>
git add . # If you want to stage ALL changes

# If you want to unstage files
git restore --staged <file>

# Next, commit those changes
git status # IF you want to see whats staged and unstaged, untracked files
git commit -m "blah blah blah i messed up"

# Now push to your branch
git push origin <your-branch-name>
```

Then on github make a pull request using your branch: https://github.com/TheTrueJM/CAB302-Exceptional-Handlers/pulls

Notes:
- You can keep working making edits after you open a PR, just keep committing and pushing your code. No need to make a new branch or anything like that
- Make sure the team gives the green flag on your changes before we merge them into then main branch
- If you stage a file then make changes, you have to stage them again with `git add`

### Handling merge conflicts
Ping us on discord

### Reverting commits (If you haven't pushed yet)
You can revert your commit by doing this
```bash
# say you commit accidentally
git commit -m "crappy code"

# crap you forgot to add something!
# just do this
git reset --soft HEAD~1

# now modify the changes you wanted to do originally
# then commit
git commit -m "happy code"
git push origin <your branch>
```
# The app

This is a maven manager app. It can manage multiple projects at once and custom settings.  

The features (planned) are:

- Nice visual manager ui on browser, with different perspectives such as:
  - Pom editor: A visual editor, making it easy to reorder entries by dragging, collapsing, autocomplete and inputs such as dropdowns or wathever majes sense.
  - Project dependencies: For many projects and modules depending on each other, gives a unified view and allow changing many projects at once.
- Allow changing multiple projects at once from the command line, for example when there is a new release.  
- Have an orchestration file(s), where the git references for all related projects and metadata can be versioned.  
- Helps investigation of where a version comes from, how it was resolved. This includes transitive versions.  
- Helps depug plugins  
- Help managing version inside a project (inter module dependency)  
- Help understand errors (for example on maven analyze failing for unused dependencies)  
- Help solve and explain tiles  
- Help editing tiles  
- Have a smart ui to edit poms, abstracting away xml and showing visual forms instead (inputs, buttons, draggable elements, etc)  
- Output reports for maven phases, what each phase did, where and how  
- Tests settings.xml , connections and correctness of poms  
- Have a command line mode and an ui mode (serving a browser)  

# Stack  

The project has these folders:

client  
A typescript+react client. It builds a website to be used locally, but that can be also installed as a PWA.  

server
A spring boot client, mixed between ui mode and command line. It uses maven libraries (it is not a cli wrapper, but uses the same libraries)  

build
Builds the client, builds the server and bundles them in a single executable jar.  
Also contains the ./run-tests.sh that reports test coverage for client and server  

wiki
A wiki in markdown format with features, use cases and general help. 

tickets
The tasks and bugs, this is a ticket system but based on markdown files.

# Documentation

On the tickets folder, give your task a number and fill the template:

```
# Task number
...
# What client asked
...
# Technical solution
...
# What changed
...
# Required Checklist !!!!
[] Test coverage is higher than 80% by running ./run-tests.sh  
[] No compile warnings or errors added.  
[] Security issues checked, listed or mitigated.  
[] Spotbugs ran, no new issues  
[] Feature is described on the wiki  
[] Double check opportunities to simplify code (less complexity, less code) , refactor and leave it simpler than you found.  
# Notes
```

Reference the task on your commit message.  
This is also used when a new release is done. Only do a new release when asked to.   

On each module, add a README.md describing the module content and its relations.  

Example (but adapt freely, just be clear about where calls come from and what are the dpepenedents and dependencies, in a general view):
```
# XYZ

This module is responsible for xyz.
It does not include abc, as it is not its responsabilities.
This module is used by abc, usually when doing the process blablabla.

It depends on module npo as it is doing blablabla2.
It usually calls nnn to do bbb.  

This was created when we needed to do xxx.

```

Every class should have a md file on the same folder with the same name but with extension md!  
This file contains all public accessible function and fieds described as pseduo code.  
The description md does not contain code and it is language agnostic. Example:  

UserValueProcessing.java
```java

class UserValueProcessing{
  public UserValueProcessing(String name, String birthdate){
    ...
  }

  public int calculateAge(Date now){
    ...
  }

  private int calculateYearDifference(){
    ...
  }
}

```

UserValueProcessing.md
```markdown
# UserValueProcessing

Used to calculate some user values, given a user state.
User details should are computed here because the logic in this case should be separate from the data.  

## Constructor

### Parameters:
- name - The full user name (name, last name)
- birthdate - The bithdate in the format YYY-MM-dd

## calculateAge

Calculates the age of the user based on todays date.

### Parameters:
- now - the current date, used to calculate the birth day difference from now
```

See that implementation detais and private fields dont show here.  
Someone that doesn't know java but is a coder should understand this file.  

Always update those md when the public contract for a class changes.  

## Wiki

Every feature needs to be documented on the wiki.  
Organize between ui mode and cli mode.  

# Development

Keep everything simple. Less code is better.  

Git and git blame is your friend.   

When adding code, if in doubt look at git blame and read the task.  

Break responsibilities in different functions and files.  

Focus on the public part of a class. A class should be usable only by reading the javadoc and looking at the public functions. The best class is the one I dont need to open the source to understand it.  

Move public functions up, the more visible and used throughout the codebase, the higher the code should appear on the file.  

Use folders to separate code by domains.  

Classes on higher folders are more visible, classes on deeper folders uses classes above. For example, if B references A and C reference A, A should be on the parent folder and B and C on child folders.

Separate by domain first and layer after. For example: Instead of folders: Components > Terminal, Tree, Folder Should be Terminal > Components Tree > Components Folder > Components

Try to keep code with less dependencies in deeper folders and code the is used on higher folders.

For example, if you have classes A B and C where B depends on A and C depends on A, A goes on parent folder and B and C goes in the child folders. The more dependencies, the higher the folder. The lower, the more independent.

Organize it in a way anyone can find the code related to a functionality looking at the folder structure. 
This means the code reflect the app functionalities. 
Add a javadoc in the header to make clear what is the file responsible for. 
Use clear variable names. Write as little code as possible, simple is better. 
Before every function always add a human-readable comment describing the function, even if it is redundant. 
Anyone should be able to recreate the function from the comment. This is a form of double bookkeeping.  
For code comments, never do useless comments that just repeat the code. Instead of:

```java
// the users count
int usersCount = 5;
```

prefer

```java
// The amount of users currently active on the database, as counted on getUserCountOnDB()
// note this may be increased after this function is returned
int currentUsersCount = 5;
store(currentUsersCount);
```


The comments should be formatted as if you where having a conversation explaining to a junior dev what a code does. 
Those are examples, feel free to vary on this:

```
    In this function we ...
    This class is responsible for ...
    This concerns ... from ...
    ...this is required because it is called from class ...
    This is a proxy ...
    This delegates ...
    This represents ...
    This is faster because ...
    This uses the data structure .... in this case because ...
    This function should ... and it does it by ..
    The way this function works is ... and it is needed because ... so we return ...
    Here we simply ... and return ...
    This class is resposible for ... this should not do x because tis is the responsability of class ...
    On this class we have all ... because having them together makes it easier to find an maintain ...
    Here we isolate the logic for ... so we don't have it on ...
    This function could be joined with ... but I decided not to do it because ... to maintain ...
    This is here to isolate code responsible for ...
    The way this works is ...
    ... and pay attention because the behavior here is ...
    ... is unusual because we need to avoid ....
```
And so on...

Always add logs, always add a trace log at the start of a function.  
Add only few info logs, only for important events or big logic branching.  

For all else, follow the decision chain:
For who am I writing the log? 
- If for Devs: Do I need to log variables? If yes is a debug, if not it is a trace
If writing logs to system operators:
- Do I log because of unwanted or unexected state? If no, it is info
- Can the process continue with the unwanted or unexected state? If yes, use warn
- Can the application continue with the unwanted or unexected state? If yes, use error, if not use fatal

About the levels:
- info: Use sparsingly, only for inportant events
- warn: Explain why the state is unwanted or unexected  
- error: Explain why the state is unwanted or unexected and consequences  
- trace: Use a lot, basically as frequent as comments, should contain the funcion, format as a narrative such as, "foo(int a, float b): now this happens with a" or "bar(): after we have value y, we do xyz to get the zyx" and so on...  
- debug: Use a lot, format as a sentence such as, "Sending int x to database" or "Mapping x to y" and so on...

Basically, info should inform me the ap is running ok, trace should give me a narraive that describe the whole flow of data and debug should besides all that give me values so I can figure out why things happens with the given values.  

Use commmon log engine, configure it so it knows the class the looger is being called from and it is easy to filter.

Always add tests. Add tests for the happy path and for errors. 
Always ask your self, what could go wrong here, then add a test to ensure the app has a good behavior.

Remember, our repo BLOCKS MERGING BRANCHES WITH FAILING TESTS. DO NOT LEAVE TESTS FAILING, FIX IT! 

All changes should have tests. Tests should have a comment describing the scenario it is testing.

Tests can be of two types:

Technical: Just test wiring or some return from a technical perspective. For example: "This function should return the color corresponding to code xyz"

Real scenario: Has a description of a real case use in the comments. For example: "User opens the terminal and types a command and expects no line breaks except the one he typed".

Important: Always update the readmes to reflect new functionality and changes on build (if applicable)!!
General code style

Parameters must be formatted as if they are a block enclosed by parenthesis. Example (java):

```java
public String foo( 
    final String bar, // This parameter contains the bar value, coming usually from the user bar choice 
    final int baz, // The amount of baz from the operation
    final float qux // qux percentage, goes from 0.0 to 1.0
) { 

}
```

Extract variables and give good names and types. Example: Instead of

```java
store(5);
```

prefer

```java
int currentUsersCount = 5;
store(currentUsersCount);
```

Try to keep the code width small.
Prefer early returns using if-return at a function start instead of if-else.
Example: Instead of

```java
if(x != null && x.specialCondition()) {
    do the thing
} else {
    log and return 
}
```

prefer
```java
if(x == null) {
    log and return
}
if(!x.specialCondition()) {
    log and return
}
do the thing
```

Think it like preparing the terrain and make sure you have all needed to do something.

Prefer streams unless it needs to much wrapping and unwrapping to work.
Avoid code that has lots of conditionals and loops in a single function.
Try to flatten the logic by doing early returns or extracting loop content to a method.
The more your code goes to the right, the worse.

Move important code to the left and keep boiler plate without line breaks. For Example, here the important thing is the name and age set: Instead of

```java
foo
  .newBuilder()
  .unwrap()
  .value()
  .setName("foobar")
  .setAge(50)
  .wrap()
  .build();
```

prefer

```java
foo.newBuilder().unwrap().value()
  .setName("foobar")
  .setAge(50)
.wrap().build();
```

See that tabulation wraps the actual important part. Someone reading can ignore the boilerplate, code is equivalent to this

```pseudocode
foo {
  .setName("foobar")
  .setAge(50)
};
```

Prefer multiple maps instead of a single logic. Example: foo.stream.map(Foo::getBar).map(Bar::getBaz) instead of foo.stream.map(f->f.getBar().getBaz())
Java code style

Extract variables and give good names.
Try to keep the code width small.
Prefer early returns using if-return at a function start instead of if-else.
Prefer streams unless it needs to much wrapping and unwrapping to work.
Prefer multiple maps instead of a single logic. Example: foo.stream.map(Foo::getBar).map(Bar::getBaz) instead of foo.stream.map(f->f.getBar().getBaz())

# Typescript code style

Clear unambiguous code. Use const by default and let when needed. Use types.  

# Cypress system dependencies

Cypress needs Xvfb and a handful of GTK/NSS libraries on Linux. Install them
before running `npm run cypress` so headless Electron can launch without the
"Missing Xvfb" error that has been plaguing CI:

```
sudo apt-get update
sudo apt-get install -y \
  libatk1.0-0 \
  libatk-bridge2.0-0 \
  libcups2 \
  libgtk-3-0 \
  libgtk2.0-0 \
  libnss3 \
  libxss1 \
  libxtst6 \
  xvfb
sudo apt-get install -y libasound2 || sudo apt-get install -y libasound2t64
```

On GitHub Actions the workflow already executes these commands, so local runs
only need them once.

# Redundancy and delimitation strategy

To keep future iterations clear and deterministic we intentionally repeat the
same idea across code, comments, documentation, tests and commit messages. This
redundancy ensures the reader can recover intent even when they only see part of
the project. We also delineate every change tightly: describe boundaries,
preconditions and invariants right next to the logic so the explored solution
space is as small as possible while still containing the correct behavior.

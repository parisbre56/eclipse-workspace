The program first reads the .spg file, parses it and creates its AST, based on the spliglet.cc grammar. 

It then uses the visitor class BlockCreatorVisitor to break down the program to to the following structure:

It processes each procedure (including MAIN) and creates a ProcedureData class for it. 
It then processes all the statements in the procedure (including LABEL and RETURN (if they exist) which are treated as separate statements for convenience's sake) and puts each of them in CodeBlockSpiglet classes, linked to the ProcedureData class mentioned earlier.

While the BlockCreatorVisitor works on doing the above, it also gathers some basic info and stores it in the appropriate class, info like:
*which vars (if any) are used in the block (and thus are definitely live in it)
*which vars (if any) are assigned a value in the block (which is used to find out when vars stop being alive), 
*which type of statement this block contains (stored as an Enumeration), *which (if any) Label is used (both on JUMP statements and in actual labels) 
*which block is connected with which block during execution (either through jump statements or fall-through links, whatever is applicable)
*the Node object associated with each statement
*if a procedure call exists in this block

Vars are processed as a special VariableData Class, which contains various useful data, such as its spiglet name, on which register or stack position the variable will be assigned to (stored as an enumeration) and whether or not a procedure call exists between its assignment and its use (because if there's a procedure call, then that means it can't be stored in tX registers, since those can be erased by the called procedure).

It is important to remember that CodeBlockSpiglet objects maintain links both to the Blocks following them and to the blocks preceding to them (so that a label can know all the jump and cjump statements pointing to it and vice versa).

It then finds the liveness of all vars by using a fixpoint loop: It keeps trying to locate the liveness of all vars for each procedure until no more changes occur. The way this is done is simple:

For each CodeBlockSpiglet of each procedure, starting from the last one, the updateLiveness method is called. This method goes through all the blocks leading to this and passes all of this' live vars to the other block's live vars, unless a var that is live in this block is assigned a value in that block, in which case it isn't added to its set of live vars. Note however that if the variable is already in that block's set of live vars (due to it being both used AND assigned a value simultaneously, for example) then it is not removed from it.

The updateLiveness method returns True if a change occurred, so that the calling function knows that it has to run the loop again to account for the new data. 

While the liveness is calculated with updateLiveness, we also see if any procedure call is made while a variable is live and if so, we mark that variable for later.

Then registers are assigned via the Linear Scan method. For each function, starting from the top block, we begin assigning variables to registers, with a preference for tX registers, so that there is a higher chance there are free sX registers for when a variable remains live during a procedure call. When there are no more registers left, the variable that will be live the longest after this block is computed with use of the this.computeLivenessHeuristic method. That variable is taken from its register and assigned a place in the stack.

vX and aX registers are not assigned, the first because they are needed for heap and stack operation and the second because they are needed for function calls.

The maximum number of arguments of procedure calls in this procedure is also computed during register allocation and stored in the ProcedureData objects.

Finally, the kanga translation begins. 

For each procedure, we first compute the number of used sX registers, so that we may know how many of them we need to temporarily store in the stack and restore in the end. This also helps us compute the stack size for the procedure decleration (the second [] field).

We then begin by writing the procedure name and the [] fields (which we have computed earlier).
Then we write the statements that store the sX vars in the stack.
Then we write the statements that move the arguments to the appropriate registers or stack positions.
Finally, we begin translating to kanga by iterating through all the blocks and using a visitor on each block's node. A special visitor is used for return statements. The visitors substitute tempvars with the appropriate vars, removing or placing an object in the stack when necessary by using vX registers.
At the end of the procedure, the statements restoring the sX registers to their original value are written.
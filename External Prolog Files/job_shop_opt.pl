:- module(jobshop_opt,[
		jobshop_opt/2
	]).

:- use_module(library(clpfd)).

:- use_module(jobshop_opt_data).

%creates a list of unnamed variables
createVarList([],0):-!.
createVarList([_|RETL],NUM):- NNUM is NUM - 1, createVarList(RETL,NNUM).

%inserts N elements E at the start of list L
insertNElements(L,_,0,L).
insertNElements(L,E,N,[E|RL]):- N>0, NEWN is N-1,insertNElements(L,E,NEWN,RL).

createJobList(LOJ):-createJobList(LOJ,[]).
createJobList(LOJ,TLJ):-job(J,_),\+member(J,TLJ),createJobList(LOJ,[J|TLJ]).
createJobList(LOJ,LOJ).

createTaskList([],[]).
createTaskList([J|LOJ],LOT):-createTaskList(LOJ,RETLOT),job(J,JLOT),append(RETLOT,JLOT,LOT).

createTaskListOfLists([],[]).
createTaskListOfLists([J|LOJ],LOLOT):-
	createTaskListOfLists(LOJ,RETLOLOT),job(J,JLOT),append(RETLOLOT,[JLOT],LOLOT).

createMachineList(LOM):-createMachineList(LOM,[]).
createMachineList(LOM,TLM):-machine(M,N),\+member(M,TLM),insertNElements(TLM,M,N,RETTLM),createMachineList(LOM,RETTLM).
createMachineList(RLOM,LOM):-!,reverse(LOM,RLOM).

jobshop_opt(Schedule, Cost):-
	createJobList(LOJ),createTaskList(LOJ,LOT),createTaskListOfLists(LOJ,LOLOT),createMachineList(LOM),!,%get data
	createConstraintsFirst(LOT,LOLOT,LOM,Varlist),
	costFunction(Varlist,Cost),!,
	createToLabelList(Varlist,ToLabel),
	labeling([min(Cost)],ToLabel),
	%create schedule simply transforms the varlist into the appropriate format 
	createSchedule(Varlist,Schedule,LOM).
	
createConstraintsFirst(LOT,LOLOT,LOM,Varlist):-
	length(LOT,NumTasks), length(LOM,NumMachines),
	NumVars is NumTasks * 4, createVarList(Varlist,NumVars),
	%creates a task list and also instantiates it so that each task gets its name (t11..t33, etc)
	createTaskList(Varlist,TaskList,LOT), 
	createStartTimeList(Varlist,StartTimeList),
	createEndTimeList(Varlist,EndTimeList),
	createMachineAssigmentList(Varlist,MachineAssigmentList),
	MachineAssigmentList ins 1 .. NumMachines,
	computeMaximumEndTime(LOT,MAXENDTIME), EndTimeList ins 1 .. MAXENDTIME ,
	!,
	taskSequenceAndTimeConstraints(LOLOT,TaskList,StartTimeList,EndTimeList,MachineAssigmentList,LOM),
	%for each time unit, constrain the number of staff
	staff(STAFF), returnMaxStaff(MAXENDTIME,TaskList,StartTimeList,EndTimeList,MaxStaffReq), MaxStaffReq #=< STAFF .
%Varlist in the form of [TASK,STARTTIME,ENDTIME,MACHINE_ASSIGMENT (note: NOT machine type, only machine number)]

computeMaximumEndTime([],0):-!.
computeMaximumEndTime([T|LOT],NMAX):- computeMaximumEndTime(LOT,OMAX), 
	task(T,_,Duration,_), NMAX is OMAX + Duration .

createTaskList([],[],[]):-!.
createTaskList([T|[_|[_|[_|VarList]]]],[T|TaskList],[T|LOT]):-createTaskList(VarList,TaskList,LOT).

createStartTimeList([],[]):-!.
createStartTimeList([_|[T|[_|[_|VarList]]]],[T|StartTimeList]):-createStartTimeList(VarList,StartTimeList).

createEndTimeList([],[]):-!.
createEndTimeList([_|[_|[T|[_|VarList]]]],[T|EndTimeList]):-createEndTimeList(VarList,EndTimeList).

createMachineAssigmentList([],[]):-!.
createMachineAssigmentList([_|[_|[_|[T|VarList]]]],[T|MachineAssigmentList]):-
	createMachineAssigmentList(VarList,MachineAssigmentList).
	
createMachineDomain(_,[],LastDomain,_,LastDomain):-!.
createMachineDomain(Machine,[Machine|LOM],N\/MDomain,N,_):-!,NEWN is N+1, 
	createMachineDomain(Machine,LOM,MDomain,NEWN,N).
createMachineDomain(Machine,[_|LOM],MDomain,N,LastDomain):-!,NEWN is N+1, 
	createMachineDomain(Machine,LOM,MDomain,NEWN,LastDomain).

constrainDoesNotOverlap(_,_,_,[],[],[]):-!.
constrainDoesNotOverlap(ThisStart,ThisEnd,ThisMachine,[Start|StartTimeList],[End|EndTimeList],[Machine|MachineAssigmentList]):-
	%if this task uses the same machine
	(ThisMachine #= Machine #/\ 
		%then this task must either end before this one or start after this one
		(ThisStart #>= End #\/ Start #>= ThisEnd) ) #\/
		%else this task must not use the same machine
		(Machine #\= ThisMachine),
	constrainDoesNotOverlap(ThisStart,ThisEnd,ThisMachine,StartTimeList,EndTimeList,MachineAssigmentList).
	

taskSequenceAndTimeConstraints([],[],[],[],[],_):-!.
taskSequenceAndTimeConstraints([[T|[T2|LOT]]|LOLOT],[T|[T2|TaskList]],[S|[S2|StartTimeList]],[E|[E2|EndTimeList]],[M|MachineAssigmentList],LOM):-
	!,
	task(T,Machine,Duration,SReq), %task(T2,Machine2,Duration2,SReq2),
	S #>= 0, E #= S + Duration, %end time is start time + duration
	createMachineDomain(Machine,LOM,MDomain,1,-1), M in MDomain, %select only appropriate machines 
	S2 #>= E, %next task must begin after the previous one ends
	%ensure no two tasks can use the same machine at the same time
	constrainDoesNotOverlap(S,E,M,[S2|StartTimeList],[E2|EndTimeList],MachineAssigmentList),
	taskSequenceAndTimeConstraints([[T2|LOT]|LOLOT],[T2|TaskList],[S2|StartTimeList],[E2|EndTimeList],MachineAssigmentList,LOM).
		
taskSequenceAndTimeConstraints([[T]|LOLOT],[T|TaskList],[S|StartTimeList],[E|EndTimeList],[M|MachineAssigmentList],LOM):-
	!,
	task(T,Machine,Duration,_),
	S #>= 0, E #= S + Duration, 
	createMachineDomain(Machine,LOM,MDomain,1,-1), M in MDomain, %select only appropriate machines
	%ensure no two tasks can use the same machine at the same time
	constrainDoesNotOverlap(S,E,M,StartTimeList,EndTimeList,MachineAssigmentList),
	taskSequenceAndTimeConstraints(LOLOT,TaskList,StartTimeList,EndTimeList,MachineAssigmentList,LOM).
	
costFunction(Varlist,Cost):-
	createEndTimeList(Varlist,EndTimeList),
	createStartTimeList(Varlist,StartTimeList),
	findMaxEnd(EndTimeList,Cost),
	findMinStart(StartTimeList,MinStart),
	MinStart #= 0 . %we are certain that we can always start a task at time 0

findMaxEnd([End],End).
findMaxEnd([End|EndList],MaxEnd):-findMaxEnd(EndList,RetMax), MaxEnd #= max(End,RetMax).

findMinStart([Start],Start).
findMinStart([Start|StartList],MinStart):-findMinStart(StartList,RetMin), MinStart #= min(Start,RetMin).

createToLabelList([],[]).
createToLabelList([_|[S|[E|[M|Varlist]]]],[S|[E|[M|ToLabel]]]):-createToLabelList(Varlist,ToLabel).

createSchedule(Varlist,Schedule,LOM):- 
	length(LOM,NumMachines), createEmptyLOL(NumMachines,ELOL),
	splitAccordingToMachine(Varlist,ELOL,RETLOL1),%puts task name, start time and end time in the appropriate machine list 
	sortAccordingToTaskStart(RETLOL1,RETLOL2),%sorts the list of tasks per machine so that each task is diplayed after its previous ones 
	createScheduleTrue(LOM,RETLOL2,Schedule).%does the actual transformation
	
createEmptyLOL(0,[]):-!.
createEmptyLOL(NUM,[[]|RETLOL]):-NNUM is NUM-1, createEmptyLOL(NNUM,RETLOL).

splitAccordingToMachine([],RETLOL,RETLOL):-!.
splitAccordingToMachine([T|[S|[E|[M|Varlist]]]],ELOL,RETLOL):-
	nth1(M,ELOL,ToChange), 
	append(ToChange,[T,S,E],Changed),
	replace(M,ELOL,Changed,NEWLOL),%replaces the Mth element of ELOL with Changed and returns it as NEWLOL
	splitAccordingToMachine(Varlist,NEWLOL,RETLOL).
	
%replaces the Mth element of ELOL with Changed and returns it as NEWLOL
replace(1,[_|ELOL],Changed,[Changed|ELOL]):-!.
replace(NUM,[ELEM|ELOL],Changed,[ELEM|NEWLOL]):- NUM > 0, NEWNUM is NUM-1, replace(NEWNUM,ELOL,Changed,NEWLOL).

sortAccordingToTaskStart([],[]):-!.
sortAccordingToTaskStart([Unsorted|ULOL],[Sorted|SLOL]):-
	bubbleSortTripletsAccordingToSecondInit(Unsorted,Sorted,>),
	sortAccordingToTaskStart(ULOL,SLOL).
	
bubbleSortTripletsAccordingToSecondInit([],[],_):-!.
bubbleSortTripletsAccordingToSecondInit(Unsorted,NewSorted,REL):-
	bubbleSortTripletsAccordingToSecond(Unsorted,Passed,REL), %make sure the final element is sorted
	append(ToSort,[TL,SL,EL],Passed), %remove the final (sorted) element from passed
	bubbleSortTripletsAccordingToSecondInit(ToSort,OldSorted,REL), %keep doing passes until all are sorted
	append(OldSorted,[TL,SL,EL],NewSorted). %put the sorted elements back in the sorted list

bubbleSortTripletsAccordingToSecond([T,S,E],[T,S,E],_):-!.
bubbleSortTripletsAccordingToSecond([T1|[S1|[E1|[T2|[S2|[E2|Varlist]]]]]],[T2|[S2|[E2|Retlist]]],>):- S1 > S2,!,
	bubbleSortTripletsAccordingToSecond([T1|[S1|[E1|Varlist]]],Retlist,>).
bubbleSortTripletsAccordingToSecond([T1|[S1|[E1|[T2|[S2|[E2|Varlist]]]]]],[T2|[S2|[E2|Retlist]]],<):- S1 < S2,!,
	bubbleSortTripletsAccordingToSecond([T1|[S1|[E1|Varlist]]],Retlist,<).
bubbleSortTripletsAccordingToSecond([T1|[S1|[E1|[T2|[S2|[E2|Varlist]]]]]],[T1|[S1|[E1|Retlist]]],REL):-
	bubbleSortTripletsAccordingToSecond([T2|[S2|[E2|Varlist]]],Retlist,REL).
	
createScheduleTrue([],[],[]):-!.
createScheduleTrue([MachineType|LOM],[LOT|LOLOT],[execs(MachineType,TransformedLOT)|TransformedLOLOT]):-
	transformLOT(LOT,TransformedLOT),
	createScheduleTrue(LOM,LOLOT,TransformedLOLOT).
	
transformLOT([],[]):-!.
transformLOT([T|[S|[E|LOT]]],[t(T,S,E)|TransformedLOT]):-transformLOT(LOT,TransformedLOT).

returnMaxStaff(0,_,_,_,0):-!.
returnMaxStaff(OLDTIME,TaskList,StartTimeList,EndTimeList,NewMaxStaffReq):-
	NEWTIME is OLDTIME - 1,
	computeStaffInTimeUnit(NEWTIME,TaskList,StartTimeList,EndTimeList,RetMaxStaffReq), %for all time units
	returnMaxStaff(NEWTIME,TaskList,StartTimeList,EndTimeList,OldMaxStaffReq), %returns the total staff requirement for the time unit
	NewMaxStaffReq #= max(OldMaxStaffReq,RetMaxStaffReq). %and then choose the greatest of them all
	
computeStaffInTimeUnit(_,[],[],[],0):-!.
computeStaffInTimeUnit(TIME,[T|TaskList],[S|StartTimeList],[E|EndTimeList],NewMaxStaffReq):-
	computeStaffInTimeUnit(TIME,TaskList,StartTimeList,EndTimeList,OldMaxStaffReq),
	task(T,_,_,TaskStaffReq),
	%if this time unit is within the time limit (S<=TIME && E>TIME)
	(((S #=< TIME) #/\ (E #> TIME) #/\
		%then add this task's staff cost to the total
		CurrentStaffReq #= TaskStaffReq )
		%else don't, make the current const 0
		#\/ (CurrentStaffReq #= 0)),
	NewMaxStaffReq #= OldMaxStaffReq + CurrentStaffReq .
		
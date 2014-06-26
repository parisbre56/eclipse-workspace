:- module(rehearsal,[
		rehearsal/2
	]).
	
:- use_module(library(clpfd)).

:- use_module(rehearsal_data).

rehearsal(Sequence, WaitTime):-
	%get data
	musicians(Musicians),durations(Durations), 
	%create vars for the sequence
	length(Durations,Length), createVarList(Sequence, Length),
	%constrain the sequence 
	Sequence ins 1 .. Length, all_different(Sequence),
	%put the cost function in as a constraint (essentially computes the
	%waitTime in clpfd constraint form)
	cost_function(Sequence,WaitTime,Musicians,Durations),
	%minimize waitTime while labeling sequence
	%once(labeling([min(WaitTime),bisect],Sequence)).
	labeling([min(WaitTime)],Sequence).


cost_function(_,0,[],_):-!.
cost_function(Sequence,NewWaitTime,[Musician|Musicians],Durations):-
	%for all musicians
	cost_function(Sequence,OldWaitTime,Musicians,Durations),
	%compute their individual cost
	musician_cost_function(0,Sequence,CurrentMusicianCost,Musician,Durations,_),
	%and add it to the total
	NewWaitTime #= OldWaitTime + CurrentMusicianCost .
	
musician_cost_function(_,[],0,_,_,0):-!.
%Musician is the list of parts of the rehearsal this musician takes part in
musician_cost_function(START,[Part|Sequence],NewMusicianCost,Musician,Durations,NEND):-
	element(Part,Musician,Rehearses),element(Part,Durations,Duration),
	%This does (if START == false && REHEARSES == true then NSTART = true, else NSTART=START)
	%this lets us know whether or not we have already encountered a 1 and thus
	%whether or not we should accept the cost of this zero.
	NSTART #= ((START-1) * -Rehearses) + START ,
	%compute oldCost. (notice that this has END and NSTART while the paren on top has NEND and START. 
	%This means that END propagates backwards while START propagates forrwards).
	musician_cost_function(NSTART,Sequence,OldMusicianCost,Musician,Durations,END),
	%Same thing for END. Notice that NEND is in the parenthesis
	NEND #= ((END-1) * -Rehearses) + END ,
	%NewCost is oldCost + Duration if START && END && !Rehearses, else 0 
	NewMusicianCost #= (START * END * (-Rehearses + 1) * Duration) + OldMusicianCost .
	
%creates a list of unnamed variables
createVarList([],0):-!.
createVarList([_|RETL],NUM):- NNUM is NUM - 1, createVarList(RETL,NNUM).





%%%%%%%%%%%%%%%%
%%%%%%TEST%%%%%%
%%%%%%%%%%%%%%%%
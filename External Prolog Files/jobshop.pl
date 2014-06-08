:- module(jobshop,[
		job_shop/1,
		job_shop_with_manpower/1
	]).

:- use_module(jobshop_data).

insertNElements(L,_,0,L).
insertNElements(L,E,N,[E|RL]):- N>0, NEWN is N-1,insertNElements(L,E,NEWN,RL).

createJobList(LOJ):-createJobList(LOJ,[]).
createJobList(LOJ,TLJ):-job(J,_),\+member(J,TLJ),createJobList(LOJ,[J|TLJ]).
createJobList(LOJ,LOJ).

createTaskList([],[]).
createTaskList([J|LOJ],LOT):-createTaskList(LOJ,RETLOT),job(J,JLOT),append(RETLOT,JLOT,LOT).

createMachineList(LOM):-createMachineList(LOM,[]).
createMachineList(LOM,TLM):-machine(M,N),\+member(M,TLM),insertNElements(TLM,M,N,RETTLM),createMachineList(LOM,RETTLM).
createMachineList(RLOM,LOM):-!,reverse(LOM,RLOM).

%A counter. Starts in 1,1,1 goes to MAX,MAX,MAX
createCNT(0,[]).
createCNT(LEN,[1|L]):- LEN>0, NLEN is LEN-1,createCNT(NLEN,L).

%increment makes 1,1,1 into 2,1,1 and MAX,MAX,1 into 1,1,2
incrementCNT([MAX|CNT],MAX,[1|NCNT]):-incrementCNT(CNT,MAX,NCNT).
incrementCNT([X|CNT],MAX,[NX|CNT]):- X>0, X<MAX, NX is X+1 .

%EXECL is a list in the form of [[t1,t2],[t3,t4],[t5,t6]] 
%where the first sublist pertains to LOM[1], the second to LOM[2] and the third to LOM[3].
assign(LOT,LOM,EXECL):-	length(LOM,NUMM),length(LOT,LENGTHLOT),
						createCNT(LENGTHLOT,CNT),assign(LOT,NUMM,CNT,EXECL,LOM).

assign(LOT,NUMM,CNT,EXECL,LOM):-applyCNT(LOT,CNT,NUMM,EXECL,LOM).
assign(LOT,NUMM,CNT,EXECL,LOM):-incrementCNT(CNT,NUMM,NCNT),!,
								assign(LOT,NUMM,NCNT,EXECL,LOM).

createEmptyLists(0,[]).
createEmptyLists(NUMM,[[]|LOL]):- NUMM>0, NNUMM is NUMM-1,createEmptyLists(NNUMM,LOL).

insertInListOfLists(LOL,ELEM,POS,RETL):-insertInListOfLists(LOL,ELEM,POS,1,RETL).

insertInListOfLists([L|LOL],ELEM,POS,POS,[[ELEM|L]|LOL]).
insertInListOfLists([L|LOL],ELEM,POS,CNT,[L|RETL]):- CNT<POS, NCNT is CNT+1, insertInListOfLists(LOL,ELEM,POS,NCNT,RETL).

%applyCNT takes an array in the form of [1,2,2,3] and [t1,t2,t3,t4] and 
%transforms it into [[t1],[t3,t2],[t4]]
%make it also create permutations in the form of [[t1],[t2,t3],[t4]]
applyCNT(LOT,CNT,NUMM,EXECL,LOM):-createEmptyLists(NUMM,LOL),applyCNT2(LOT,CNT,LOL,EXECL,LOM).

applyCNT2([],[],LOL,EXECL,LOM):-valid(LOL,LOM),permutateLOL(LOL,EXECL).
applyCNT2([T|LOT],[POS|CNT],LOL,EXECL,LOM):-insertInListOfLists(LOL,T,POS,NLOL),applyCNT2(LOT,CNT,NLOL,EXECL,LOM).

%permutateLOL applies a permutation to every list in the list of lists
permutateLOL([],[]).
permutateLOL([L|LOL],[PL|PLOL]):-permutation(L,PL),permutateLOL(LOL,PLOL).

job_shop(S):-createJobList(LOJ),createTaskList(LOJ,LOT),createMachineList(LOM),!, %gets data
			assign(LOT,LOM,EXECL), %test all possible assigments to machines
			convert(EXECL,LOM,S), 
			%convert to requested format while performing 
			%more test and creating more permutations
			true. %used for debugging
			
valid([],[]).
valid([LOT|LOLOT],[M|LOM]):-valid2(LOT,M),valid(LOLOT,LOM).

valid2(LOT,M):-valid2(LOT,M,0).

valid2([],_,TIME):-deadline(DEAD),TIME=<DEAD.
valid2([T|LOT],M,TIME):-task(T,M,TTIME),NTIME is TIME+TTIME,valid2(LOT,M,NTIME).

convert([],[],[]).
convert([LOT|LOLOT],[M|LOM],[S|SL]):-convert2(LOT,M,S,0),convert(LOLOT,LOM,SL).

convert2(LOT,M,execs(M,LOTCON),TIME):-convert3(LOT,M,TIME,LOTCON).
%convert2(LOT,M,execs(M,LOTCON),TIME):-NTIME is TIME+1, deadline(DEAD), computeTime(LOT,M,TTTIME), TTTIME+NTIME=<DEAD, convert2(LOT,M,execs(M,LOTCON),NTIME).

convert3([],_,_,[]).
convert3([T|LOT],M,TIME,[t(T,TIME,NTIME)|LOTCON]):-task(T,M,TTIME),NTIME is TIME+TTIME,convert3(LOT,M,NTIME,LOTCON).
convert3([T|LOT],M,TIME,LOTCON):-NTIME is TIME+1, deadline(DEAD), computeTime([T|LOT],M,TTTIME), TTTIME+NTIME=<DEAD, convert3([T|LOT],M,NTIME,LOTCON).

computeTime([],_,0).
computeTime([T|LOT],M,TIME):-task(T,M,TTIME),computeTime(LOT,M,RETTIME),TIME is RETTIME+TTIME.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

assignMAN(LOT,LOM,EXECL):-	length(LOM,NUMM),length(LOT,LENGTHLOT),
							createCNT(LENGTHLOT,CNT),assignMAN(LOT,NUMM,CNT,EXECL,LOM).

assignMAN(LOT,NUMM,CNT,EXECL,LOM):-applyCNTMAN(LOT,CNT,NUMM,EXECL,LOM).
assignMAN(LOT,NUMM,CNT,EXECL,LOM):-incrementCNT(CNT,NUMM,NCNT),!,
									assignMAN(LOT,NUMM,NCNT,EXECL,LOM).
								
applyCNTMAN(LOT,CNT,NUMM,EXECL,LOM):-createEmptyLists(NUMM,LOL),applyCNT2MAN(LOT,CNT,LOL,EXECL,LOM).

applyCNT2MAN([],[],LOL,EXECL,LOM):-validMAN(LOL,LOM),permutateLOL(LOL,EXECL).
applyCNT2MAN([T|LOT],[POS|CNT],LOL,EXECL,LOM):-insertInListOfLists(LOL,T,POS,NLOL),applyCNT2MAN(LOT,CNT,NLOL,EXECL,LOM).

job_shop_with_manpower(S):-	createJobList(LOJ),createTaskList(LOJ,LOT),createMachineList(LOM),!, %creates data
							assignMAN(LOT,LOM,EXECL), %test all possible assigments to machines
							convertMAN(EXECL,LOM,S),
							testMan(S),
							true.
							
validMAN([],[]).
validMAN([LOT|LOLOT],[M|LOM]):-valid2MAN(LOT,M),validMAN(LOLOT,LOM).

valid2MAN(LOT,M):-valid2MAN(LOT,M,0).

valid2MAN([],_,TIME):-deadline(DEAD),TIME=<DEAD.
valid2MAN([T|LOT],M,TIME):-task(T,M,TTIME,_),NTIME is TIME+TTIME,valid2MAN(LOT,M,NTIME).

convertMAN([],[],[]).
convertMAN([LOT|LOLOT],[M|LOM],[execs(M,LOTCON)|SL]):-convert2MAN(LOT,M,LOTCON,0),convertMAN(LOLOT,LOM,SL).

convert2MAN(LOT,M,LOTCON,TIME):-convert3MAN(LOT,M,TIME,LOTCON).
%convert2MAN(LOT,M,execs(M,LOTCON),TIME):-NTIME is TIME+1, deadline(DEAD), computeTimeMAN(LOT,M,TTTIME), TTTIME+NTIME=<DEAD, convert2MAN(LOT,M,execs(M,LOTCON),NTIME).

convert3MAN([],_,_,[]).
convert3MAN([T|LOT],M,TIME,[t(T,TIME,NTIME)|LOTCON]):-task(T,M,TTIME,_),NTIME is TIME+TTIME,convert3MAN(LOT,M,NTIME,LOTCON).
convert3MAN([T|LOT],M,TIME,LOTCON):-NTIME is TIME+1, deadline(DEAD), computeTimeMAN([T|LOT],M,TTTIME), TTTIME+NTIME=<DEAD, convert3MAN([T|LOT],M,NTIME,LOTCON).

computeTimeMAN([],_,0).
computeTimeMAN([T|LOT],M,TIME):-task(T,M,TTIME,_),computeTimeMAN(LOT,M,RETTIME),TIME is RETTIME+TTIME.

%checks whether or not manpower is exceeded at any time during execution..
testMan(S):-testMan(S,0).

%for all time units
testMan(_,TIME):-deadline(DEAD),TIME>DEAD.
testMan(S,TIME):-deadline(DEAD),TIME=<DEAD,testMan2(S,TIME,0),NTIME is TIME+1,testMan(S,NTIME).

%test all machines for this time unit
	%this ensures that it will fail EVERY time MEN>DMEN
testMan2(_,_,MEN):-staff(DMEN),MEN>DMEN,!,fail.
	%tested all machines
testMan2([],_,_).
	%tested all tasks necessary for this machine
testMan2([execs(_,[t(_,STARTTIME,_)|_])|SL],TIME,MEN):-TIME<STARTTIME,!,testMan2(SL,TIME,MEN).
testMan2([execs(_,[])|SL],TIME,MEN):-!,testMan2(SL,TIME,MEN).
	%task for this machine happens in time unit
testMan2([execs(M,[t(T,STARTTIME,ENDTIME)|_])|SL],TIME,MEN):- STARTTIME =< TIME, TIME < ENDTIME, !,
										task(T,M,_,TMEN), NMEN is MEN+TMEN, %add men to total
										testMan2(SL,TIME,NMEN). %check next machine
	%task before time unit
testMan2([execs(M,[t(_,_,ENDTIME)|LOT])|SL],TIME,MEN):- TIME>=ENDTIME,!,
										testMan2([execs(M,LOT)|SL],TIME,MEN). %check next task
:- module(matropers,[
		matr_transp/2,
		matr_mult/3,
		matr_det/2
	]).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

listFirst([], [], []).
listFirst([[X|L1]|L2], [X|L3], [L1|L4]) :- listFirst(L2, L3, L4).

matr_transp([],[]).
matr_transp([X|L1], TL) :- matr_transp(X, [X|L1], TL).
matr_transp([], _, []).
matr_transp([_|L1], L2, [X|L3]) :- listFirst(L2, X, LF), matr_transp(L1, LF, L3).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

dotProduct([],[],0).
dotProduct([A|LA],[B|LB],C):-dotProduct(LA,LB,C2),C1 is A*B,C is C1+C2.

multiplyAction2(_,[],[]).
multiplyAction2(A,[B|LB],C):-dotProduct(A,B,C1),multiplyAction2(A,LB,C2),C=[C1|C2].

multiplyAction([],_,[]).
multiplyAction([A|LA],LB,C):-multiplyAction2(A,LB,C1),multiplyAction(LA,LB,C2),C=[C1|C2].

matr_mult([],[],[]).
matr_mult(A,B,C):-matr_transp(B,BT),multiplyAction(A,BT,C).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

detAction([X],LEFT,_,[],RET):- matr_transp(LEFT,RETL),matr_det(RETL,RET1),RET is X*RET1.
detAction([X|L],LEFT,MISSING,[Y|RIGHT],RET):- 
									append(LEFT,[MISSING],APP),
									detActionMinus(L,APP,Y,RIGHT,RET1),
									append(LEFT,[Y|RIGHT],TOTRANS),
									matr_transp(TOTRANS,RETL),
									matr_det(RETL,RET2),
									RET is RET1+RET2*X.
												
detActionMinus([X],LEFT,_,[],RET):- matr_transp(LEFT,RETL),matr_det(RETL,RET1),RET is X*(-RET1).
detActionMinus([X|L],LEFT,MISSING,[Y|RIGHT],RET):- 
									append(LEFT,[MISSING],APP),
									detAction(L,APP,Y,RIGHT,RET1),
									append(LEFT,[Y|RIGHT],TOTRANS),
									matr_transp(TOTRANS,RETL),
									matr_det(RETL,RET2),
									RET is RET1-RET2*X.

matr_det([],1).
matr_det([[A]],A).
matr_det([[A,B],[C,D]],RET):- RET is A*D - B*C .
matr_det([X|LOL],DT):- matr_transp(LOL,[X1|L]),detAction(X,[],X1,L,DT).
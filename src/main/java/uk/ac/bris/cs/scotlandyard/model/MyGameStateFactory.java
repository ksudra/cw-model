        public ImmutableSet<Piece> getWinner() {
            List<Piece> winners = new ArrayList<>();
            ImmutableSet<Move> AvailableMoves = getAvailableMoves();
         if(AvailableMoves.isEmpty()){
             if(!remaining.contains(mrX.piece())){
                 winners.add(mrX.piece());
             }else if(remaining.contains(mrX.piece())){
                 for (int i = 0; i < detectives.size(); i++)
                             winners.add(detectives.get(i).piece());
                         }
         }
            return ImmutableSet.copyOf(winners);
        }

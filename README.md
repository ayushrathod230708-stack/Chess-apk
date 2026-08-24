# Personal Chess Coach — Native Android/Kotlin

A Jetpack Compose MVP for:
- playing on an in-app chessboard
- automatic move recording
- basic mistake-pattern tracking
- game report
- opening strategy selection

## Build
1. Install Android Studio.
2. Open this folder.
3. Allow Gradle sync.
4. Use JDK 17.
5. Build > Build APK(s).

## Important MVP note
The included tracker is functional but heuristic-based. A production-quality blunder/mistake engine should integrate Stockfish and a complete chess rules engine (legal moves, check/checkmate, castling, promotion, en passant). The UI and tracking architecture are prepared to extend in that direction.

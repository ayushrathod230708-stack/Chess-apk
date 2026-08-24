package com.ayush.personalchesscoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { ChessCoachApp() } }
    }
}

data class MoveRecord(val move: String, val category: String, val advice: String)
data class Opening(val name:String, val moves:List<String>, val plan:String)

private val openings = listOf(
    Opening("Italian Game", listOf("e4","e5","Nf3","Nc6","Bc4"), "Develop quickly, control the centre and castle early."),
    Opening("London System", listOf("d4","d5","Nf3","Nf6","Bf4"), "Build a solid structure and develop safely."),
    Opening("Caro-Kann Defence", listOf("e4","c6","d4","d5"), "Challenge the centre with a solid pawn structure.")
)

@Composable
fun ChessCoachApp() {
    var tab by remember { mutableIntStateOf(0) }
    var moves by remember { mutableStateOf(listOf<MoveRecord>()) }
    var opening by remember { mutableStateOf(openings[0]) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf("Play","Report","Openings").forEachIndexed { i, title ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i },
                        icon = { Text(if(i==0) "♟" else if(i==1) "📊" else "📖") },
                        label = { Text(title) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when(tab) {
                0 -> PlayScreen(opening, moves) { moves = moves + it }
                1 -> ReportScreen(moves)
                2 -> OpeningScreen(opening) { opening = it }
            }
        }
    }
}

@Composable
fun PlayScreen(opening: Opening, moves: List<MoveRecord>, onMove: (MoveRecord)->Unit) {
    var selected by remember { mutableStateOf<String?>(null) }
    var ply by remember { mutableIntStateOf(moves.size) }
    val pieces = remember { mutableStateMapOf<String,String>().apply {
        val back = listOf("♜","♞","♝","♛","♚","♝","♞","♜")
        ('a'..'h').forEachIndexed { i, f ->
            this["$f8"] = back[i]; this["$f7"]="♟"; this["$f2"]="♙"; this["$f1"] = listOf("♖","♘","♗","♕","♔","♗","♘","♖")[i]
        }
    }}
    Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Personal Chess Coach", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
        Text("Opening plan: ${opening.name}")
        Spacer(Modifier.height(8.dp))
        Column(Modifier.aspectRatio(1f).fillMaxWidth()) {
            for (rank in 8 downTo 1) {
                Row(Modifier.weight(1f)) {
                    for (file in 'a'..'h') {
                        val square = "$file$rank"
                        val light = ((file.code-'a'.code)+rank)%2==0
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .background(if(light) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    val piece = pieces[square]
                                    if(selected == null && piece != null) selected = square
                                    else if(selected != null) {
                                        val from = selected!!
                                        val moving = pieces[from] ?: return@clickable
                                        val notation = "$from-$square"
                                        pieces.remove(from); pieces[square]=moving; selected=null; ply++
                                        onMove(MistakeTracker.analyze(notation, moving, square, ply, opening))
                                    }
                                },
                            contentAlignment=Alignment.Center
                        ) { Text(piece ?: "", fontSize=30.sp) }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Tap a piece, then tap its destination. This MVP tracks moves; full legal-move validation and Stockfish can be added as the next engine module.")
    }
}

object MistakeTracker {
    fun analyze(move:String, piece:String, to:String, ply:Int, opening:Opening): MoveRecord {
        val category: String
        val advice: String
        when {
            ply <= 10 && !opening.moves.any { it.contains(to.take(1), ignoreCase=true) } -> {
                category="Inaccuracy"; advice="Opening: follow your repertoire plan—develop pieces, control the centre and castle."
            }
            piece in listOf("♕","♛") && ply < 8 -> {
                category="Mistake"; advice="Your queen moved very early. Usually develop knights and bishops first."
            }
            to.endsWith("1") || to.endsWith("8") -> {
                category="Good move"; advice="Check that the piece is safe and look for your opponent's threats."
            }
            else -> {
                category="Move recorded"; advice="Before every move: checks, captures, threats, and piece safety."
            }
        }
        return MoveRecord(move, category, advice)
    }
}

@Composable
fun ReportScreen(moves: List<MoveRecord>) {
    val inaccuracies = moves.count { it.category=="Inaccuracy" }
    val mistakes = moves.count { it.category=="Mistake" }
    val good = moves.count { it.category=="Good move" }
    val weakness = when {
        mistakes > inaccuracies -> "Early queen development / opening discipline"
        inaccuracies > 0 -> "Opening strategy and piece development"
        moves.isEmpty() -> "Play a game to build your profile"
        else -> "Continue playing to collect more data"
    }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp)) {
        item {
            Text("Your Mistake Tracker", style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text("Moves tracked: ${moves.size}")
            Text("🟢 Good moves: $good")
            Text("🟡 Inaccuracies: $inaccuracies")
            Text("🟠 Mistakes: $mistakes")
            Spacer(Modifier.height(16.dp))
            Card { Column(Modifier.padding(16.dp)) {
                Text("Current biggest weakness", fontWeight=FontWeight.Bold)
                Text(weakness)
                Text("Training rule: before moving, check opponent checks, captures and threats.")
            }}
            Spacer(Modifier.height(12.dp))
        }
        items(moves.size) { i ->
            val m=moves[i]
            ListItem(headlineContent={Text(m.move+" — "+m.category)}, supportingContent={Text(m.advice)})
        }
    }
}

@Composable
fun OpeningScreen(selected: Opening, onSelect:(Opening)->Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp)) {
        item { Text("Opening Strategy", style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold); Spacer(Modifier.height(12.dp)) }
        items(openings.size) { i ->
            val o=openings[i]
            Card(Modifier.fillMaxWidth().padding(vertical=6.dp).clickable { onSelect(o) }) {
                Column(Modifier.padding(16.dp)) {
                    Text(o.name, fontWeight=FontWeight.Bold)
                    Text(o.moves.joinToString(" → "))
                    Text(o.plan)
                    if(o.name==selected.name) Text("✓ Current strategy")
                }
            }
        }
    }
}

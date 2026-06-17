package com.example.appviagem.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.appviagem.data.local.entity.Foto
import com.example.appviagem.viewmodel.FotoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun GaleriaFotosScreen(
    navController: NavHostController,
    viagemId: Int,
    destino: String
) {
    val context = LocalContext.current
    val fotoViewModel: FotoViewModel = viewModel(factory = FotoViewModel.Factory(context))
    val fotos by fotoViewModel.fotos.collectAsState()

    var mostrarOpcoes by remember { mutableStateOf(false) }
    var fotoParaExcluir by remember { mutableStateOf<Foto?>(null) }

    // Caminho do arquivo aguardando captura pela camera
    var caminhoCaptura by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viagemId) {
        fotoViewModel.carregarFotos(viagemId)
    }

    // Launcher: capturar foto com a camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { sucesso ->
        if (sucesso) {
            caminhoCaptura?.let { fotoViewModel.vincularFotoCapturada(it) }
        } else {
            // Captura cancelada: remove arquivo vazio
            caminhoCaptura?.let { runCatching { File(it).delete() } }
        }
        caminhoCaptura = null
    }

    fun abrirCamera() {
        val arquivo = fotoViewModel.criarArquivoCaptura()
        caminhoCaptura = arquivo.absolutePath
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            arquivo
        )
        cameraLauncher.launch(uri)
    }

    // Permissao de camera
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA) { granted ->
        if (granted) abrirCamera()
    }

    // Launcher: selecionar foto da galeria do dispositivo
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { fotoViewModel.vincularFotoDaGaleria(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fotos da Viagem")
                        Text(
                            text = destino,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarOpcoes = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Foto")
            }
        }
    ) { innerPadding ->
        if (fotos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nenhuma foto adicionada",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toque no botão + para adicionar fotos da galeria ou da câmera",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(items = fotos, key = { it.id }) { foto ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { fotoParaExcluir = foto }
                            )
                    ) {
                        AsyncImage(
                            model = File(foto.caminho),
                            contentDescription = "Foto da viagem",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }

    // Folha de opcoes: galeria ou camera
    if (mostrarOpcoes) {
        ModalBottomSheet(onDismissRequest = { mostrarOpcoes = false }) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Adicionar foto",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
                ListItem(
                    headlineContent = { Text("Escolher da galeria") },
                    leadingContent = {
                        Icon(Icons.Default.Place, contentDescription = null)
                    },
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).combinedClickable(
                        onClick = {
                            mostrarOpcoes = false
                            galeriaLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onLongClick = {}
                    )
                )
                ListItem(
                    headlineContent = { Text("Tirar foto com a câmera") },
                    leadingContent = {
                        Icon(Icons.Default.AddCircle, contentDescription = null)
                    },
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).combinedClickable(
                        onClick = {
                            mostrarOpcoes = false
                            if (cameraPermission.status.isGranted) {
                                abrirCamera()
                            } else {
                                cameraPermission.launchPermissionRequest()
                            }
                        },
                        onLongClick = {}
                    )
                )
            }
        }
    }

    // Dialog de confirmacao de exclusao
    if (fotoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { fotoParaExcluir = null },
            title = { Text("Excluir Foto") },
            text = { Text("Deseja realmente excluir esta foto da viagem?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        fotoParaExcluir?.let { fotoViewModel.excluir(it) }
                        fotoParaExcluir = null
                    }
                ) {
                    Text("Excluir", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { fotoParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

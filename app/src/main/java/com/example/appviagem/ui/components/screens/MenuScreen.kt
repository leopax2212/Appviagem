package com.example.appviagem.ui.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appviagem.viewmodel.AuthViewModel
import com.example.appviagem.viewmodel.LocationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MenuScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // LocationViewModel
    val locationViewModel: LocationViewModel = viewModel(
        factory = LocationViewModel.Factory(context)
    )
    val locationState by locationViewModel.uiState.collectAsStateWithLifecycle()

    // Permissao de localizacao
    val locationPermission = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    ) { granted ->
        if (granted) locationViewModel.onPermissionGranted()
    }

    // Solicitar permissao ao iniciar
    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            locationViewModel.onPermissionGranted()
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    // Fecha o app ao pressionar voltar no Menu
    BackHandler {
        (context as? Activity)?.finish()
    }

    val user = authViewModel.loggedUser
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                // Header do Drawer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = user?.nome ?: "Usuario",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Menu Items
                DrawerMenuItem(
                    icon = Icons.Default.Add,
                    label = "Nova Viagem",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("nova_viagem")
                    }
                )

                DrawerMenuItem(
                    icon = Icons.Default.List,
                    label = "Minhas Viagens",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("minhas_viagens")
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                DrawerMenuItem(
                    icon = Icons.Default.Info,
                    label = "Sobre",
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("sobre")
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Logout
                DrawerMenuItem(
                    icon = Icons.Default.ExitToApp,
                    label = "Sair",
                    onClick = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("App Viagem") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Saudacao
                Text(
                    text = "Bem-vindo(a),",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = user?.nome ?: "Usuario",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card de Localizacao
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sua Localizacao",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when {
                            locationPermission.status.shouldShowRationale -> {
                                Text(
                                    text = "Permissao de localizacao necessaria",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { locationPermission.launchPermissionRequest() }
                                ) {
                                    Text("Conceder Permissao")
                                }
                            }

                            locationState.isLoading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Obtendo localizacao...")
                                }
                            }

                            locationState.city != null -> {
                                Text(
                                    text = "Cidade: ${locationState.city}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            else -> {
                                Text(
                                    text = "Localizacao nao disponivel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card de Viagem Atual (se encontrada)
                locationState.viagemAtual?.let { viagem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (viagem.tipo == "lazer")
                                        Icons.Default.Star
                                    else
                                        Icons.Default.Build,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Viagem Atual",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Destino
                            InfoRow(
                                label = "Destino",
                                value = viagem.destino
                            )

                            // Tipo
                            InfoRow(
                                label = "Tipo",
                                value = if (viagem.tipo == "lazer") "Lazer" else "Negocios"
                            )

                            // Data Inicio
                            InfoRow(
                                label = "Data Inicio",
                                value = dateFormatter.format(Date(viagem.dataInicio))
                            )

                            // Data Fim
                            InfoRow(
                                label = "Data Fim",
                                value = dateFormatter.format(Date(viagem.dataFim))
                            )

                            // Orcamento
                            InfoRow(
                                label = "Orcamento",
                                value = currencyFormatter.format(viagem.orcamento)
                            )

                            // Total de Gastos
                            InfoRow(
                                label = "Total de Gastos",
                                value = currencyFormatter.format(locationState.totalGastos)
                            )
                        }
                    }
                }

                // Mensagem quando nao ha viagem para a cidade
                if (locationState.city != null && locationState.viagemAtual == null && !locationState.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma viagem ativa para ${locationState.city}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cards de acesso rapido
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.Add,
                        title = "Nova Viagem",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("nova_viagem") }
                    )

                    QuickAccessCard(
                        icon = Icons.Default.List,
                        title = "Minhas Viagens",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("minhas_viagens") }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
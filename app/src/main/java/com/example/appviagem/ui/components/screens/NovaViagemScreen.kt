package com.example.appviagem.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appviagem.viewmodel.AuthViewModel
import com.example.appviagem.viewmodel.ViagemViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaViagemScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    viagemViewModel: ViagemViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    // DatePicker states
    var showDatePickerInicio by remember { mutableStateOf(false) }
    var showDatePickerFim by remember { mutableStateOf(false) }

    val isEdicao = viagemViewModel.viagemEmEdicao != null

    LaunchedEffect(viagemViewModel.successMessage) {
        if (viagemViewModel.successMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(viagemViewModel.successMessage)
            viagemViewModel.successMessage = ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEdicao) "Editar Viagem" else "Nova Viagem") },
                navigationIcon = {
                    IconButton(onClick = {
                        viagemViewModel.limparCampos()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Destino
            OutlinedTextField(
                value = viagemViewModel.destino,
                onValueChange = { viagemViewModel.destino = it },
                label = { Text("Destino") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tipo de Viagem
            Text(
                text = "Tipo de Viagem",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TipoViagemOption(
                    text = "Lazer",
                    selected = viagemViewModel.tipo == "lazer",
                    onClick = { viagemViewModel.tipo = "lazer" },
                    modifier = Modifier.weight(1f)
                )

                TipoViagemOption(
                    text = "Negócios",
                    selected = viagemViewModel.tipo == "negocios",
                    onClick = { viagemViewModel.tipo = "negocios" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Início
            OutlinedTextField(
                value = viagemViewModel.dataInicio?.let { dateFormatter.format(Date(it)) } ?: "",
                onValueChange = { },
                label = { Text("Data de Início") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePickerInicio = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Data Fim
            OutlinedTextField(
                value = viagemViewModel.dataFim?.let { dateFormatter.format(Date(it)) } ?: "",
                onValueChange = { },
                label = { Text("Data de Fim") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePickerFim = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Orçamento
            OutlinedTextField(
                value = viagemViewModel.orcamento,
                onValueChange = { viagemViewModel.orcamento = it },
                label = { Text("Orçamento (R$)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Salvar
            Button(
                onClick = {
                    authViewModel.loggedUser?.let { user ->
                        viagemViewModel.salvar(user.id) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEdicao) "Atualizar" else "Salvar")
            }

            // Erro
            if (viagemViewModel.errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = viagemViewModel.errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // DatePicker Início
    if (showDatePickerInicio) {
        DatePickerDialog(
            onDismiss = { showDatePickerInicio = false },
            onDateSelected = { millis ->
                viagemViewModel.dataInicio = millis
                showDatePickerInicio = false
            }
        )
    }

    // DatePicker Fim
    if (showDatePickerFim) {
        DatePickerDialog(
            onDismiss = { showDatePickerFim = false },
            onDateSelected = { millis ->
                viagemViewModel.dataFim = millis
                showDatePickerFim = false
            }
        )
    }
}

@Composable
private fun TipoViagemOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = selected,
                onClick = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
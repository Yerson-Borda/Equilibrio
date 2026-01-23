@file:Suppress("UNUSED_EXPRESSION")

package com.example.moneymate.ui.screens.transaction.addtransaction

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.category.model.Category
import com.example.domain.tag.model.Tag
import com.example.domain.wallet.model.Wallet
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.components.states.SectionStateManager
import com.example.moneymate.ui.screens.transaction.component.TransactionTextField
import com.example.moneymate.utils.IconMapper
import com.example.moneymate.utils.ScreenState
import org.koin.androidx.compose.koinViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTransactionScreen(
    onBackClick: () -> Unit,
    onAddTransaction: () -> Unit,
    viewModel: AddTransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val uriStrings = uris.map { it.toString() }
                viewModel.onAttachmentsSelected(uriStrings)
            }
        }
    )

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateHome -> {
                onAddTransaction()
                viewModel.clearNavigationEvent()
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.transactionState) {
        if (uiState.transactionState is ScreenState.Error) {
            val error = (uiState.transactionState as ScreenState.Error).error
            Toast.makeText(context, error.getUserFriendlyMessage(), Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color(0xFFF4F4F4),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Text(
                    text = "Add Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        viewModel.createTransaction(context)
                    },
                    enabled = uiState.transactionState !is ScreenState.Loading &&
                            uiState.selectedWalletId != 0 &&
                            uiState.amount != "0" &&
                            uiState.amount != "0." &&
                            (uiState.selectedType != TransactionType.TRANSFER ||
                                    (uiState.destinationWalletId != 0 &&
                                            uiState.destinationWalletId != uiState.selectedWalletId)),
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color(0xFFF4F4F4),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.transactionState is ScreenState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.walletsState is ScreenState.Loading &&
                    uiState.categoriesState is ScreenState.Loading &&
                    uiState.tagsState is ScreenState.Loading -> {
                FullScreenLoading(message = "Loading transaction data...")
            }
            uiState.walletsState is ScreenState.Error -> {
                FullScreenError(
                    error = (uiState.walletsState as ScreenState.Error).error,
                    onRetry = { viewModel.loadWallets() }
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    TransactionTypeSelector(
                        selectedType = uiState.selectedType,
                        onTypeSelected = viewModel::onTransactionTypeSelected
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "$${uiState.amount}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(25.dp))
                    when (uiState.selectedType) {
                        TransactionType.TRANSFER -> {
                            TransferContent(
                                uiState = uiState,
                                walletsState = uiState.walletsState,
                                onSourceWalletSelected = { wallet ->
                                    viewModel.onWalletSelected(
                                        walletId = wallet.id ?: 0,
                                        walletName = wallet.name
                                    )
                                },
                                onDestinationWalletSelected = { wallet ->
                                    viewModel.onDestinationWalletSelected(
                                        walletId = wallet.id ?: 0,
                                        walletName = wallet.name
                                    )
                                },
                                onNoteChanged = viewModel::onNoteChanged,
                                viewModel = viewModel,
                                onAddAttachment = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                        else -> {
                            IncomeExpenseContent(
                                uiState = uiState,
                                walletsState = uiState.walletsState,
                                categoriesState = uiState.categoriesState,
                                onWalletSelected = { wallet ->
                                    viewModel.onWalletSelected(
                                        walletId = wallet.id ?: 0,
                                        walletName = wallet.name
                                    )
                                },
                                onCategorySelected = { categoryId, categoryName ->
                                    viewModel.onCategorySelected(categoryId, categoryName)
                                },
                                onNoteChanged = viewModel::onNoteChanged,
                                onNameChanged = viewModel::onNameChanged,
                                viewModel = viewModel,
                                onAddAttachment = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))
                    when (uiState.tagsState) {
                        is ScreenState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        is ScreenState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Tags",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray.copy(alpha = 0.1f))
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "Failed to load tags",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(33.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4D6BFA),
                                        contentColor = Color.White
                                    ),
                                    shape = RectangleShape
                                ) {
                                    Text(
                                        text = "Create New Tag",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        is ScreenState.Empty -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Tags",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.LightGray.copy(alpha = 0.1f))
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "No tags available",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        else -> {
                            TagsSection(
                                availableTags = uiState.availableTags,
                                selectedTagIds = uiState.selectedTagIds,
                                onTagSelected = viewModel::onTagSelected,
                                onCreateTag = viewModel::onCreateTag
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    NumberPad(
                        onNumberPressed = viewModel::onNumberPressed,
                        onBackspacePressed = viewModel::onBackspacePressed,
                        onDecimalPressed = viewModel::onDecimalPressed
                    )
                }
            }
        }
    }
}

@Composable
fun TransferContent(
    uiState: AddTransactionState,
    walletsState: ScreenState<List<Wallet>>,
    onAddAttachment: () -> Unit,
    onSourceWalletSelected: (Wallet) -> Unit,
    onDestinationWalletSelected: (Wallet) -> Unit,
    onNoteChanged: (String) -> Unit,
    viewModel: AddTransactionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // --- 1. Simple Currency Conversion Row ---
        val hasAmount = uiState.amount.isNotEmpty() && uiState.amount != "0" && uiState.amount != "0."
        val currenciesAreDifferent = uiState.sourceWalletCurrency != uiState.destinationWalletCurrency

        if (currenciesAreDifferent && hasAmount && uiState.transferPreview != null) {
            val preview = uiState.transferPreview

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estimated Total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    
                    if (uiState.isLoadingPreview) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF1E88E5)
                        )
                    } else {
                        Text(
                            text = "${com.example.moneymate.utils.CurrencyUtils.getCurrencySymbol(preview.destinationCurrency)}${"%.2f".format(preview.convertedAmount)} ${preview.destinationCurrency}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5)
                        )
                    }
                }

                // Small details row underneath
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Exchange rate: ${"%.4f".format(preview.exchangeRate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4CAF50),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                    Text(
                        text = "From: ${uiState.amount} ${preview.sourceCurrency}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // --- 2. Wallets Selection Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SectionStateManager(
                    state = walletsState,
                    onRetry = { viewModel.loadWallets() }
                ) { wallets ->
                    WalletDropdown(
                        selectedWalletName = uiState.selectedWalletName,
                        wallets = wallets,
                        onWalletSelected = onSourceWalletSelected,
                        label = "From Wallet"
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                SectionStateManager(
                    state = walletsState,
                    onRetry = { viewModel.loadWallets() }
                ) { wallets ->
                    WalletDropdown(
                        selectedWalletName = uiState.destinationWalletName,
                        wallets = wallets.filter { it.id != uiState.selectedWalletId },
                        onWalletSelected = onDestinationWalletSelected,
                        label = "To Wallet"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // --- 3. Note Field ---
        TransactionTextField(
            Type = "Add Note",
            text = uiState.note,
            onValueChanged = onNoteChanged
        )

        Spacer(modifier = Modifier.height(15.dp))

        // --- 4. Attachment Button ---
        SimpleAttachmentButton(
            onAddAttachment = onAddAttachment,
            modifier = Modifier.fillMaxWidth()
        )

        // --- 5. Attachments Preview ---
        if (uiState.attachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            SelectedAttachmentsSection(
                attachments = uiState.attachments,
                onRemoveAttachment = { uri ->
                    viewModel.removeAttachment(uri)
                }
            )
        }
    }
}

@Composable
fun IncomeExpenseContent(
    uiState: AddTransactionState,
    walletsState: ScreenState<List<Wallet>>,
    categoriesState: ScreenState<List<Category>>,
    onAddAttachment: () -> Unit,
    onWalletSelected: (Wallet) -> Unit,
    onCategorySelected: (Int, String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    viewModel: AddTransactionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                SectionStateManager(
                    state = walletsState,
                    onRetry = { viewModel.loadWallets() }
                ) { wallets ->
                    WalletDropdown(
                        selectedWalletName = uiState.selectedWalletName,
                        wallets = wallets,
                        onWalletSelected = onWalletSelected,
                        label = "Wallet"
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                SectionStateManager(
                    state = categoriesState,
                    onRetry = { viewModel.loadCategories() }
                ) { categories ->
                    CategoryDropdown(
                        selectedCategoryName = uiState.selectedCategoryName,
                        categories = categories,
                        onCategorySelected = onCategorySelected
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TransactionTextField(
                    Type = "Name",
                    text = uiState.name,
                    onValueChanged = onNameChanged
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                SimpleAttachmentButton(
                    onAddAttachment = onAddAttachment,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TransactionTextField(
            Type = "Add Note",
            text = uiState.note,
            onValueChanged = onNoteChanged
        )

        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.attachments.isNotEmpty()) {
            SelectedAttachmentsSection(
                attachments = uiState.attachments,
                onRemoveAttachment = { uri ->
                    viewModel.removeAttachment(uri)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SimpleAttachmentButton(
    onAddAttachment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = " ",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Transparent,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { onAddAttachment() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Attachment",
                    tint = Color(0xFF29A073),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add Attachment",
                    color = Color(0xFF29A073),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SelectedAttachmentsSection(
    attachments: List<String>,
    onRemoveAttachment: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Selected Attachments (${attachments.size})",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attachments) { uri ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Image",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { onRemoveAttachment(uri) },
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WalletDropdown(
    selectedWalletName: String,
    wallets: List<Wallet>,
    onWalletSelected: (Wallet) -> Unit,
    label: String = "Wallet"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedWalletName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = Color.Gray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (wallets.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No wallets available") },
                    onClick = { expanded = false }
                )
            } else {
                wallets.forEach { wallet ->
                    DropdownMenuItem(
                        text = {
                            Text("${wallet.name} - $${wallet.balance ?: "0.00"}")
                        },
                        onClick = {
                            onWalletSelected(wallet)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDropdown(
    selectedCategoryName: String,
    categories: List<Category>,
    onCategorySelected: (Int, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    ) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedCategory = categories.find { it.name == selectedCategoryName }
            if (selectedCategory != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                IconMapper.getBackgroundColor(selectedCategory.color)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = IconMapper.getIconDrawable(selectedCategory.icon)
                            ),
                            contentDescription = selectedCategory.name,
                            colorFilter = ColorFilter.tint(
                                IconMapper.parseColor(selectedCategory.color)
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = selectedCategory.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = selectedCategoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                tint = Color.Gray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (categories.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No categories available") },
                    onClick = { expanded = false }
                )
            } else {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Icon in circle for dropdown items
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(
                                            IconMapper.getBackgroundColor(category.color)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(
                                            id = IconMapper.getIconDrawable(category.icon)
                                        ),
                                        contentDescription = category.name,
                                        colorFilter = ColorFilter.tint(
                                            IconMapper.parseColor(category.color)
                                        ),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        onClick = {
                            onCategorySelected(category.id, category.name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TransactionType.values().forEach { type ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .height(48.dp)
                    .background(
                        color = if (selectedType == type) {
                            when (type) {
                                TransactionType.INCOME -> Color(0xFFCEE3F5)
                                TransactionType.EXPENSE -> Color(0xFF4D6BFA)
                                TransactionType.TRANSFER -> Color(0xFFCEE3F5)
                            }
                        } else {
                            Color.LightGray.copy(alpha = 0.3f)
                        }
                    )
                    .clickable { onTypeSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.displayName,
                    color = if (selectedType == type) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun TagsSection(
    availableTags: List<Tag>,
    selectedTagIds: List<Int>,
    onTagSelected: (Int) -> Unit,
    onCreateTag: (String) -> Unit
) {
    var newTagText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Tags",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableTags) { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = if (selectedTagIds.contains(tag.id)) Color(
                                0xFF6FBAFC
                            ) else Color(0x40F8F8F8)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onTagSelected(tag.id) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#${tag.name}",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        BasicTextField(
                            value = newTagText,
                            onValueChange = { newTagText = it },
                            modifier = Modifier.width(80.dp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (newTagText.isEmpty()) {
                                        Text(
                                            text = "New Tag",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newTagText.isNotBlank()) {
                                        onCreateTag(newTagText)
                                        newTagText = ""
                                    }
                                }
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
        Button(
            onClick = {
                if (newTagText.isNotBlank()) {
                    onCreateTag(newTagText)
                    newTagText = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(33.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4D6BFA),
                contentColor = Color.White
            ),
            shape = RectangleShape
        ) {
            Text(
                text = "Insert Template",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NumberPad(
    onNumberPressed: (String) -> Unit,
    onBackspacePressed: () -> Unit,
    onDecimalPressed: () -> Unit
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "←")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        numbers.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { number ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                when (number) {
                                    "←" -> onBackspacePressed()
                                    "." -> onDecimalPressed()
                                    else -> onNumberPressed(number)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
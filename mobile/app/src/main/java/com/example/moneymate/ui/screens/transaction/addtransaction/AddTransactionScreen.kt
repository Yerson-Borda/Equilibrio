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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.category.model.Category
import com.example.domain.wallet.model.Wallet
import com.example.moneymate.ui.components.states.FullScreenError
import com.example.moneymate.ui.components.states.FullScreenLoading
import com.example.moneymate.ui.components.states.SectionStateManager
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

    // Add gallery launcher inside the composable
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val uriStrings = uris.map { it.toString() }
                viewModel.onAttachmentsSelected(uriStrings)
            }
        }
    )

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateHome -> {
                onAddTransaction()
                viewModel.clearNavigationEvent()
            }
            else -> {}
        }
    }

    // Handle transaction state errors (show as Toast)
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
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }

                Text(
                    text = "Add Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        viewModel.createTransaction()
                    },
                    enabled = uiState.transactionState !is ScreenState.Loading &&
                            uiState.selectedWalletId != 0 &&
                            uiState.amount != "0" &&
                            uiState.amount != "0." &&
                            (uiState.selectedType != TransactionType.TRANSFER ||
                                    (uiState.destinationWalletId != 0 &&
                                            uiState.destinationWalletId != uiState.selectedWalletId))
                ) {
                    if (uiState.transactionState is ScreenState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Main content with state management
        when {
            // Show full screen loading if both wallets and categories are loading
            uiState.walletsState is ScreenState.Loading && uiState.categoriesState is ScreenState.Loading -> {
                FullScreenLoading(message = "Loading transaction data...")
            }
            // Show full screen error if wallets failed to load (critical data)
            uiState.walletsState is ScreenState.Error -> {
                FullScreenError(
                    error = (uiState.walletsState as ScreenState.Error).error,
                    onRetry = { viewModel.loadWallets() }
                )
            }
            // Show normal content
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(paddingValues)
                ) {
                    // Transaction Type Selector
                    TransactionTypeSelector(
                        selectedType = uiState.selectedType,
                        onTypeSelected = viewModel::onTransactionTypeSelected
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Amount Display
                    Text(
                        text = "$${uiState.amount}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Dynamic Content based on Transaction Type
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
                                onDescriptionChanged = viewModel::onDescriptionChanged,
                                viewModel = viewModel,
                                onAddAttachment = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                        else -> { // INCOME or EXPENSE
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
                                onDescriptionChanged = viewModel::onDescriptionChanged,
                                viewModel = viewModel,
                                onAddAttachment = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tags Section (common for all types)
                    TagsSection(
                        selectedTags = uiState.selectedTags,
                        onTagSelected = viewModel::onTagSelected,
                        onNewTagAdded = { newTag ->
                            viewModel.onTagSelected(newTag)
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Number Pad (common for all types)
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
    onDescriptionChanged: (String) -> Unit,
    viewModel: AddTransactionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // From Wallet with state management
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

        Spacer(modifier = Modifier.height(16.dp))

        // To Wallet with state management
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

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        DescriptionTextField(
            description = uiState.description,
            onDescriptionChanged = onDescriptionChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Attachments
        AttachmentsSection(
            attachments = uiState.attachments,
            onAddAttachment = onAddAttachment,
            onRemoveAttachment = { uri ->
                viewModel.removeAttachment(uri)
            }
        )
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
    onDescriptionChanged: (String) -> Unit,
    viewModel: AddTransactionViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Wallet with state management
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

        Spacer(modifier = Modifier.height(16.dp))

        // Category with state management
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

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        DescriptionTextField(
            description = uiState.description,
            onDescriptionChanged = onDescriptionChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Attachments
        AttachmentsSection(
            attachments = uiState.attachments,
            onAddAttachment = onAddAttachment,
            onRemoveAttachment = { uri ->
                viewModel.removeAttachment(uri)
            }
        )
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
            // Display selected category with icon in circle
            val selectedCategory = categories.find { it.name == selectedCategoryName }
            if (selectedCategory != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon in circle background
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (selectedType == type) {
                            when (type) {
                                TransactionType.INCOME -> Color(0xFF4CAF50)
                                TransactionType.EXPENSE -> Color(0xFFF44336)
                                TransactionType.TRANSFER -> Color(0xFF2196F3)
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
fun DescriptionTextField(
    description: String,
    onDescriptionChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Add Description",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    if (description.isEmpty()) {
                        Text(
                            text = "Enter description...",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray)
        )
    }
}

@Composable
fun AttachmentsSection(
    attachments: List<String>,
    onAddAttachment: () -> Unit,
    onRemoveAttachment: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Attachments",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Add Attachment Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onAddAttachment() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Attachment",
                    tint = Color.Blue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Attachment",
                    color = Color.Blue,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Display selected attachments
        if (attachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
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
                        // Here you would display the image thumbnail
                        // For now, showing a placeholder
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

                        // Remove button
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
}

@Composable
fun TagsSection(
    selectedTags: List<String>,
    onTagSelected: (String) -> Unit,
    onNewTagAdded: (String) -> Unit
) {
    var newTagText by remember { mutableStateOf("") }

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

        val defaultTags = listOf("#work", "#bonus")

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(defaultTags) { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = if (selectedTags.contains(tag)) Color.Blue else Color.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onTagSelected(tag) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = tag,
                        color = if (selectedTags.contains(tag)) Color.Blue else Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            // New Tag input
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(16.dp)
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
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Insert Template",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Blue,
            modifier = Modifier.clickable { /* Handle template insertion */ }
        )
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
        horizontalAlignment = Alignment.CenterHorizontally
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
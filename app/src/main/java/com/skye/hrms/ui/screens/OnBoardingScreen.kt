package com.skye.hrms.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skye.hrms.data.viewmodels.EducationItem
import com.skye.hrms.data.viewmodels.OnboardingFormData
import com.skye.hrms.data.viewmodels.OnboardingViewModel
import com.skye.hrms.data.viewmodels.LeaveSubmissionState
import java.text.SimpleDateFormat
import java.util.*

private fun validateStep(step: Int, formData: OnboardingFormData): Set<String> {
    val errors = mutableSetOf<String>()
    when (step) {
        1 -> {
            if (formData.fullName.isBlank()) errors.add("fullName")
            if (formData.dateOfBirth.isBlank()) errors.add("dateOfBirth")
            if (formData.gender.isBlank()) errors.add("gender")
        }
        2 -> {
            if (formData.contactNumber.isBlank()) errors.add("contactNumber")
            if (formData.currentAddress.isBlank()) errors.add("currentAddress")
            if (!formData.isPermanentAddressSameAsCurrent && formData.permanentAddress.isBlank()) errors.add("permanentAddress")
        }
        3 -> {
            // No mandatory fields in this step in the original code
        }
        4 -> {
            formData.educationalHistory.forEach {
                if (it.degree.isBlank()) errors.add("degree_${it.id}")
                if (it.university.isBlank()) errors.add("university_${it.id}")
                if (it.year.isBlank()) errors.add("year_${it.id}")
            }
        }
        5 -> {
            if (formData.emergencyContactName.isBlank()) errors.add("emergencyContactName")
            if (formData.emergencyContactNumber.isBlank()) errors.add("emergencyContactNumber")
        }
    }
    return errors
}

@Composable
fun OnBoardingScreen(
    onboardingViewModel: OnboardingViewModel = viewModel(),
    onFormSubmitted: () -> Unit
) {
    val currentStep by onboardingViewModel.currentStep.collectAsState()
    val formData by onboardingViewModel.formData.collectAsState()
    val submissionState by onboardingViewModel.submissionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var validationErrors by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(submissionState) {
        when (val state = submissionState) {
            is LeaveSubmissionState.Success -> onFormSubmitted()
            is LeaveSubmissionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                onboardingViewModel.resetSubmissionState()
            }
            else -> {}
        }
    }

    val handleNext = {
        val errors = validateStep(currentStep, formData)
        if (errors.isEmpty()) {
            onboardingViewModel.onNextStep()
        } else {
            validationErrors = errors
        }
    }

    val handleSubmit = {
        val errors = validateStep(currentStep, formData)
        if (errors.isEmpty()) {
            onboardingViewModel.submitForm()
        } else {
            validationErrors = errors
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ProgressStepper(currentStep = currentStep, totalSteps = 5)
            },
            bottomBar = {
                FormNavigationBar(
                    currentStep = currentStep,
                    onPrevious = onboardingViewModel::onPreviousStep,
                    onNext = handleNext,
                    onSubmit = handleSubmit,
                    isLoading = submissionState is LeaveSubmissionState.Loading
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            AnimatedContent(
                targetState = currentStep,
                modifier = Modifier.padding(paddingValues),
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    }.using(SizeTransform(clip = false))
                },
                label = "Onboarding Step Animation"
            ) { step ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    item {
                        when (step) {
                            1 -> PersonalDetailsStep(formData = formData, viewModel = onboardingViewModel, validationErrors = validationErrors, clearError = { validationErrors = validationErrors - it })
                            2 -> ContactDetailsStep(formData = formData, viewModel = onboardingViewModel, validationErrors = validationErrors, clearError = { validationErrors = validationErrors - it })
                            3 -> EmploymentDetailsStep(formData = formData, viewModel = onboardingViewModel)
                            4 -> AcademicDetailsStep(formData = formData, viewModel = onboardingViewModel, validationErrors = validationErrors, clearError = { validationErrors = validationErrors - it })
                            5 -> DependentsDetailsStep(formData = formData, viewModel = onboardingViewModel, validationErrors = validationErrors, clearError = { validationErrors = validationErrors - it })
                        }
                    }
                }
            }
        }

        if (submissionState is LeaveSubmissionState.Loading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ProgressStepper(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..totalSteps) {
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            val color by animateColorAsState(
                targetValue = when {
                    isCompleted -> MaterialTheme.colorScheme.primary
                    isCurrent -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }, label = "Step Color Animation"
            )
            val contentColor by animateColorAsState(
                targetValue = when {
                    isCompleted -> MaterialTheme.colorScheme.onPrimary
                    isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }, label = "Step Content Color Animation"
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = "Completed", tint = contentColor)
                } else {
                    Text(text = "$step", color = contentColor, fontWeight = FontWeight.Bold)
                }
            }

            if (step < totalSteps) {
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FormNavigationBar(
    currentStep: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    Surface(shadowElevation = 8.dp, tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                    Spacer(Modifier.width(8.dp))
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Button(
                onClick = if (currentStep == 5) onSubmit else onNext,
                enabled = !isLoading,
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text(if (currentStep == 5) "Submit" else "Next")
                Spacer(Modifier.width(8.dp))
                if (currentStep < 5) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                } else {
                    Icon(Icons.Default.Check, contentDescription = "Submit")
                }
            }
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit,
    isError: Boolean,
    clearError: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            onDateSelected(format.format(calendar.time))
            clearError()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedTextField(
        value = date,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        isError = isError,
        supportingText = { if (isError) Text("This field is required", color = MaterialTheme.colorScheme.error) },
        modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.CalendarToday, "Date Picker")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel, validationErrors: Set<String>, clearError: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader(title = "Personal Details", subtitle = "Let's start with the basics.")
        OutlinedTextField(
            value = formData.fullName,
            onValueChange = {
                viewModel.updateFullName(it)
                clearError("fullName")
            },
            label = { Text("Full Name") },
            isError = "fullName" in validationErrors,
            supportingText = { if ("fullName" in validationErrors) Text("Full name is required", color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        DatePickerField(
            label = "Date of Birth",
            date = formData.dateOfBirth,
            onDateSelected = viewModel::updateDateOfBirth,
            isError = "dateOfBirth" in validationErrors,
            clearError = { clearError("dateOfBirth") }
        )
        var genderExpanded by remember { mutableStateOf(false) }
        val genderOptions = listOf("Male", "Female", "Other")
        ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = !genderExpanded }) {
            OutlinedTextField(
                value = formData.gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                isError = "gender" in validationErrors,
                supportingText = { if ("gender" in validationErrors) Text("Please select a gender", color = MaterialTheme.colorScheme.error) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        viewModel.updateGender(option)
                        clearError("gender")
                        genderExpanded = false
                    })
                }
            }
        }
    }
}

@Composable
private fun ContactDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel, validationErrors: Set<String>, clearError: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader(title = "Contact Information", subtitle = "How can we reach you?")
        OutlinedTextField(
            value = formData.contactNumber,
            onValueChange = {
                viewModel.updateContactNumber(it)
                clearError("contactNumber")
            },
            label = { Text("Contact Number") },
            isError = "contactNumber" in validationErrors,
            supportingText = { if ("contactNumber" in validationErrors) Text("Contact number is required", color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = formData.currentAddress,
            onValueChange = {
                viewModel.updateCurrentAddress(it)
                clearError("currentAddress")
            },
            label = { Text("Current Address") },
            isError = "currentAddress" in validationErrors,
            supportingText = { if ("currentAddress" in validationErrors) Text("Current address is required", color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            minLines = 3
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = formData.isPermanentAddressSameAsCurrent, onCheckedChange = viewModel::togglePermanentAddressSameAsCurrent)
            Text("Permanent address is same as current", style = MaterialTheme.typography.bodyMedium)
        }
        AnimatedVisibility(visible = !formData.isPermanentAddressSameAsCurrent) {
            OutlinedTextField(
                value = formData.permanentAddress,
                onValueChange = {
                    viewModel.updatePermanentAddress(it)
                    clearError("permanentAddress")
                },
                label = { Text("Permanent Address") },
                isError = "permanentAddress" in validationErrors,
                supportingText = { if ("permanentAddress" in validationErrors) Text("Permanent address is required", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 3
            )
        }
    }
}

@Composable
private fun EmploymentDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader(title = "Employment Details", subtitle = "Tell us about your role.")
        OutlinedTextField(value = formData.employeeId, onValueChange = viewModel::updateEmployeeId, label = { Text("Employee ID") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
        OutlinedTextField(value = formData.designation, onValueChange = viewModel::updateDesignation, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
        OutlinedTextField(value = formData.department, onValueChange = viewModel::updateDepartment, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true)
        DatePickerField(label = "Date of Joining", date = formData.dateOfJoining, onDateSelected = viewModel::updateDateOfJoining, isError = false, clearError = {})
    }
}

@Composable
private fun AcademicDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel, validationErrors: Set<String>, clearError: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader(title = "Academic Background", subtitle = "Add your qualifications.")
        formData.educationalHistory.forEach { item ->
            EducationItemCard(
                item = item,
                onItemChange = { updatedItem -> viewModel.updateEducationItem(updatedItem) },
                onRemove = { viewModel.removeEducationItem(item) },
                canRemove = formData.educationalHistory.size > 1,
                validationErrors = validationErrors,
                clearError = clearError
            )
        }
        Button(
            onClick = viewModel::addEducationItem,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, "Add")
            Spacer(Modifier.width(8.dp))
            Text("Add Another Qualification")
        }
    }
}

@Composable
fun EducationItemCard(item: EducationItem, onItemChange: (EducationItem) -> Unit, onRemove: () -> Unit, canRemove: Boolean, validationErrors: Set<String>, clearError: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = item.degree,
                onValueChange = {
                    onItemChange(item.copy(degree = it))
                    clearError("degree_${item.id}")
                },
                label = { Text("Degree/Certificate") },
                isError = "degree_${item.id}" in validationErrors,
                supportingText = { if ("degree_${item.id}" in validationErrors) Text("Required", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = item.university,
                onValueChange = {
                    onItemChange(item.copy(university = it))
                    clearError("university_${item.id}")
                },
                label = { Text("University/Board") },
                isError = "university_${item.id}" in validationErrors,
                supportingText = { if ("university_${item.id}" in validationErrors) Text("Required", color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(value = item.specialisation, onValueChange = { onItemChange(item.copy(specialisation = it)) }, label = { Text("Specialisation") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(
                value = item.year,
                onValueChange = {
                    onItemChange(item.copy(year = it))
                    clearError("year_${item.id}")
                },
                label = { Text("Year of Completion") },
                isError = "year_${item.id}" in validationErrors,
                supportingText = { if ("year_${item.id}" in validationErrors) Text("Required", color = MaterialTheme.colorScheme.error) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (canRemove) {
                IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun DependentsDetailsStep(formData: OnboardingFormData, viewModel: OnboardingViewModel, validationErrors: Set<String>, clearError: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepHeader(title = "Emergency Contact", subtitle = "Who should we contact in an emergency?")
        OutlinedTextField(
            value = formData.emergencyContactName,
            onValueChange = {
                viewModel.updateEmergencyContactName(it)
                clearError("emergencyContactName")
            },
            label = { Text("Emergency Contact Name") },
            isError = "emergencyContactName" in validationErrors,
            supportingText = { if ("emergencyContactName" in validationErrors) Text("Name is required", color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = formData.emergencyContactNumber,
            onValueChange = {
                viewModel.updateEmergencyContactNumber(it)
                clearError("emergencyContactNumber")
            },
            label = { Text("Emergency Contact Number") },
            isError = "emergencyContactNumber" in validationErrors,
            supportingText = { if ("emergencyContactNumber" in validationErrors) Text("Number is required", color = MaterialTheme.colorScheme.error) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}
